package fr.techad.sonar;

import fr.techad.sonar.gerrit.GerritFacade;
import fr.techad.sonar.gerrit.factory.GerritFacadeFactory;
import fr.techad.sonar.gerrit.review.ReviewFileComment;
import fr.techad.sonar.gerrit.review.ReviewInput;
import fr.techad.sonar.gerrit.review.ReviewLineComment;
import fr.techad.sonar.mockito.MockitoExtension;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.sonar.api.batch.fs.InputPath;
import org.sonar.api.batch.postjob.PostJobContext;
import org.sonar.api.batch.postjob.PostJobDescriptor;
import org.sonar.api.batch.postjob.issue.PostJobIssue;
import org.sonar.api.batch.rule.Severity;
import org.sonar.api.config.internal.MapSettings;
import org.sonar.api.rule.RuleKey;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TECH ADVANTAGE
 * All right reserved
 * Created by cochon on 31/07/2018.
 */
@ExtendWith(MockitoExtension.class)
@RunWith(JUnitPlatform.class)
class GerritPostJobTest {
    private static final String LABEL = "Code-Review";
    private static final String PATH1 = "src/main/java/F1.java";
    private static final String PATH2 = "src/main/java/F2.java";

    @Captor
    ArgumentCaptor<String> describeStringCaptor;
    @Captor
    ArgumentCaptor<ReviewInput> reviewInputCaptor;

    private MapSettings settings;

    @BeforeEach
    public void setUp() {
        ReviewHolder.getReviewInput().emptyComments();
        // Common Settings
        settings = new MapSettings();
        settings.setProperty(PropertyKey.GERRIT_SCHEME, GerritConstants.SCHEME_HTTP)
            .setProperty(PropertyKey.GERRIT_HOST, "localhost")
            .appendProperty(PropertyKey.GERRIT_PORT, "10800")
            .setProperty(PropertyKey.GERRIT_PROJECT, "project")
            .setProperty(PropertyKey.GERRIT_CHANGE_ID, "changeid")
            .setProperty(PropertyKey.GERRIT_REVISION_ID, "revisionid")
            .setProperty(PropertyKey.GERRIT_VOTE_NO_ISSUE, "1")
            .setProperty(PropertyKey.GERRIT_VOTE_ISSUE_ABOVE_THRESHOLD, "-2")
            .setProperty(PropertyKey.GERRIT_VOTE_ISSUE_BELOW_THRESHOLD, "-1")
            .setProperty(PropertyKey.GERRIT_ENABLED, "true")
            .setProperty(PropertyKey.GERRIT_MESSAGE, "Message Test")
            .setProperty(PropertyKey.GERRIT_ISSUE_COMMENT, "[New: ${issue.isNew}] ${issue.severity}(${issue.ruleKey}) found: ${issue.message}")
            .setProperty(PropertyKey.GERRIT_LABEL, LABEL);
    }

    @Test
    void shouldDescribeTheJob() {
        GerritConfiguration gerritConfiguration = mock(GerritConfiguration.class);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        PostJobDescriptor descriptor = mock(PostJobDescriptor.class);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.describe(descriptor);
        verify(descriptor).name(describeStringCaptor.capture());
        String name = describeStringCaptor.getValue();
        verify(descriptor).requireProperty(describeStringCaptor.capture());
        String property = describeStringCaptor.getValue();
        Assertions.assertEquals("GERRIT PLUGIN", name);
        Assertions.assertEquals(PropertyKey.GERRIT_CHANGE_ID, property);
    }

    @Test
    public void shouldCallOneListFilesOnAssertOrFetchGerritModifiedFiles() throws GerritPluginException {
        GerritConfiguration gerritConfiguration = mock(GerritConfiguration.class);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        when(gerritFacade.listFiles()).thenReturn(new ArrayList<>());

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.assertOrFetchGerritModifiedFiles();
        gerritPostJob.assertOrFetchGerritModifiedFiles();
        verify(gerritFacade, times(1)).listFiles();
    }

    @Test
    public void shouldConvertIssueToComment() {
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);

        RuleKey ruleKey = RuleKey.of("test-repo", "sonar-1");
        PostJobIssue postJobIssue = mock(PostJobIssue.class);
        when(postJobIssue.line()).thenReturn(12);
        when(postJobIssue.severity()).thenReturn(Severity.INFO);
        when(postJobIssue.message()).thenReturn("Should be a message test");
        when(postJobIssue.ruleKey()).thenReturn(ruleKey);
        when(postJobIssue.isNew()).thenReturn(Boolean.TRUE);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        ReviewLineComment reviewLineComment = gerritPostJob.issueToComment(postJobIssue);
        Assertions.assertEquals(new Integer(12), reviewLineComment.getLine());
        Assertions.assertEquals(0, reviewLineComment.getSeverity());
        Assertions.assertEquals("[New: true] INFO(test-repo:sonar-1) found: Should be a message test", reviewLineComment.getMessage());
    }

    @Test
    public void shouldDoNothingIfThePluginIsDisabled() throws GerritPluginException {
        GerritConfiguration gerritConfiguration = mock(GerritConfiguration.class);
        when(gerritConfiguration.isEnabled()).thenReturn(Boolean.FALSE);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        PostJobContext postJobContext = mock(PostJobContext.class);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);
        verify(gerritFacade, never()).setReview(any());
    }

    @Test
    public void shouldSendNoIssueOnExecute() throws GerritPluginException {
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(new ArrayList<>());

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(1, integer.intValue());
        Assertions.assertEquals(0, reviewInput.getComments().size());
    }

    @Test
    public void shouldSendBelowMsgIssueOnExecute() throws GerritPluginException {
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "BLOCKER");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(PATH1);
        listFiles.add(PATH2);
        when(gerritFacade.listFiles()).thenReturn(listFiles);

        InputPath inputPath1 = createInputPath(PATH1, Boolean.TRUE);
        InputPath inputPath2 = createInputPath(PATH2, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.INFO, "R1", "Msg 1", Boolean.TRUE));
        postJobIssues.add(createPostJobIssueMock(20, inputPath1, Severity.MAJOR, "R2", "Msg 2", Boolean.TRUE));
        postJobIssues.add(createPostJobIssueMock(1020, inputPath2, Severity.INFO, "R3", "Msg 3", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        // Vote Value
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(-1, integer.intValue());
        Map<String, List<ReviewFileComment>> inputComments = reviewInput.getComments();
        Assertions.assertEquals(2, inputComments.size());
        List<ReviewFileComment> reviewFileComments = inputComments.get(PATH1);
        Assertions.assertEquals(2, reviewFileComments.size());
        Assertions.assertEquals(0, reviewFileComments.get(0).getSeverity());
        ReviewLineComment reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(10, reviewLineComment.getLine().intValue());
        Assertions.assertEquals(2, reviewFileComments.get(1).getSeverity());
        reviewLineComment = (ReviewLineComment) reviewFileComments.get(1);
        Assertions.assertEquals(20, reviewLineComment.getLine().intValue());
        reviewFileComments = inputComments.get(PATH2);
        Assertions.assertEquals(1, reviewFileComments.size());
        Assertions.assertEquals(0, reviewFileComments.get(0).getSeverity());
        reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(1020, reviewLineComment.getLine().intValue());
    }

    @Test
    public void shouldSendAboveMsgIssueOnExecute() throws GerritPluginException {
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(PATH1);
        listFiles.add(PATH2);
        when(gerritFacade.listFiles()).thenReturn(listFiles);

        InputPath inputPath1 = createInputPath(PATH1, Boolean.TRUE);
        InputPath inputPath2 = createInputPath(PATH2, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        postJobIssues.add(createPostJobIssueMock(20, inputPath1, Severity.MAJOR, "R2", "Msg 2", Boolean.TRUE));
        postJobIssues.add(createPostJobIssueMock(1020, inputPath2, Severity.MAJOR, "R3", "Msg 3", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        // Vote Value
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(-2, integer.intValue());
        Map<String, List<ReviewFileComment>> inputComments = reviewInput.getComments();
        Assertions.assertEquals(2, inputComments.size());
        List<ReviewFileComment> reviewFileComments = inputComments.get(PATH1);
        Assertions.assertEquals(2, reviewFileComments.size());
        Assertions.assertEquals(2, reviewFileComments.get(0).getSeverity());
        ReviewLineComment reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(10, reviewLineComment.getLine().intValue());
        Assertions.assertEquals(2, reviewFileComments.get(1).getSeverity());
        reviewLineComment = (ReviewLineComment) reviewFileComments.get(1);
        Assertions.assertEquals(20, reviewLineComment.getLine().intValue());
        reviewFileComments = inputComments.get(PATH2);
        Assertions.assertEquals(1, reviewFileComments.size());
        Assertions.assertEquals(2, reviewFileComments.get(0).getSeverity());
        reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(1020, reviewLineComment.getLine().intValue());
    }

    @Test
    public void shouldCommentNewIssueOnly() throws GerritPluginException {
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        settings.setProperty(PropertyKey.GERRIT_COMMENT_NEW_ISSUES_ONLY, "true");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(PATH1);
        when(gerritFacade.listFiles()).thenReturn(listFiles);

        InputPath inputPath1 = createInputPath(PATH1, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        postJobIssues.add(createPostJobIssueMock(20, inputPath1, Severity.BLOCKER, "R2", "Msg 2", Boolean.FALSE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        // Vote Value
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(-2, integer.intValue());
        Map<String, List<ReviewFileComment>> inputComments = reviewInput.getComments();
        Assertions.assertEquals(1, inputComments.size());
        List<ReviewFileComment> reviewFileComments = inputComments.get(PATH1);
        Assertions.assertEquals(1, reviewFileComments.size());
        Assertions.assertEquals(2, reviewFileComments.get(0).getSeverity());
        ReviewLineComment reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(10, reviewLineComment.getLine().intValue());
    }

    @Test
    public void shouldFoundFilenameWithPrependValue() throws GerritPluginException {
        String path1WithProject = "P1/" + PATH1;
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(PATH1);
        when(gerritFacade.listFiles()).thenReturn(listFiles);
        when(gerritFacade.parseFileName(path1WithProject)).thenReturn(PATH1);

        InputPath inputPath1 = createInputPath(path1WithProject, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        // Vote Value
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(-2, integer.intValue());
        Map<String, List<ReviewFileComment>> inputComments = reviewInput.getComments();
        Assertions.assertEquals(1, inputComments.size());
        List<ReviewFileComment> reviewFileComments = inputComments.get(PATH1);
        Assertions.assertEquals(1, reviewFileComments.size());
        Assertions.assertEquals(2, reviewFileComments.get(0).getSeverity());
        ReviewLineComment reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(10, reviewLineComment.getLine().intValue());
    }

    @Test
    public void shouldFoundFilenameByParsingGerritFileList() throws GerritPluginException {
        String path1WithProject = "P1/" + PATH1;
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(path1WithProject);
        when(gerritFacade.listFiles()).thenReturn(listFiles);
        when(gerritFacade.parseFileName(path1WithProject)).thenReturn(PATH1);

        InputPath inputPath1 = createInputPath(PATH1, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        // Vote Value
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(-2, integer.intValue());
        Map<String, List<ReviewFileComment>> inputComments = reviewInput.getComments();
        Assertions.assertEquals(1, inputComments.size());
        List<ReviewFileComment> reviewFileComments = inputComments.get(path1WithProject);
        Assertions.assertEquals(1, reviewFileComments.size());
        Assertions.assertEquals(2, reviewFileComments.get(0).getSeverity());
        ReviewLineComment reviewLineComment = (ReviewLineComment) reviewFileComments.get(0);
        Assertions.assertEquals(10, reviewLineComment.getLine().intValue());
    }

    @Test
    public void shouldIgnoreIssueWhenNotFoundFilename() throws GerritPluginException {
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(PATH1);
        when(gerritFacade.listFiles()).thenReturn(listFiles);
        when(gerritFacade.parseFileName(PATH1)).thenReturn(PATH1);

        InputPath inputPath1 = createInputPath(PATH2, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(1, integer.intValue());
        Assertions.assertEquals(0, reviewInput.getComments().size());
    }

    @Test
    public void shouldIgnoreIssueWhenFileTypeIsNotFile() throws GerritPluginException {
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        List<String> listFiles = new ArrayList<>();
        listFiles.add(PATH1);
        when(gerritFacade.listFiles()).thenReturn(listFiles);
        when(gerritFacade.parseFileName(PATH1)).thenReturn(PATH1);

        InputPath inputPath1 = createInputPath(PATH1, Boolean.FALSE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(1, integer.intValue());
        Assertions.assertEquals(0, reviewInput.getComments().size());
    }

    @Test
    public void shouldCatchThrownExceptionDuringGetListFileFromGerrit() throws GerritPluginException {
        settings.setProperty(PropertyKey.GERRIT_THRESHOLD, "INFO");
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);
        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        when(gerritFacade.listFiles()).thenThrow(new GerritPluginException("Test"));

        InputPath inputPath1 = createInputPath(PATH1, Boolean.TRUE);

        List<PostJobIssue> postJobIssues = new ArrayList<>();
        postJobIssues.add(createPostJobIssueMock(10, inputPath1, Severity.MAJOR, "R1", "Msg 1", Boolean.TRUE));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(postJobIssues);

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);

        verify(gerritFacade).setReview(reviewInputCaptor.capture());
        ReviewInput reviewInput = reviewInputCaptor.getValue();
        Map<String, Integer> labels = reviewInput.getLabels();
        Assertions.assertEquals(1, labels.size());
        Integer integer = labels.get(LABEL);
        Assertions.assertNotNull(integer);
        Assertions.assertEquals(1, integer.intValue());
        Assertions.assertEquals(0, reviewInput.getComments().size());


    }

    @Test
    public void shouldCatchThrownExceptionDuringExecute() throws GerritPluginException {
        GerritConfiguration gerritConfiguration = new GerritConfiguration(settings);

        GerritFacadeFactory gerritFacadeFactory = mock(GerritFacadeFactory.class);
        GerritFacade gerritFacade = mock(GerritFacade.class);
        when(gerritFacadeFactory.getFacade()).thenReturn(gerritFacade);
        doThrow(new GerritPluginException("Test")).when(gerritFacade).setReview(any(ReviewInput.class));
        PostJobContext postJobContext = mock(PostJobContext.class);
        when(postJobContext.issues()).thenReturn(new ArrayList<>());

        GerritPostJob gerritPostJob = new GerritPostJob(settings, gerritConfiguration, gerritFacadeFactory);
        gerritPostJob.execute(postJobContext);
    }

    private PostJobIssue createPostJobIssueMock(int line, InputPath inputPath, Severity severity, String rule, String msg, Boolean isNew) {
        PostJobIssue postJobIssue = mock(PostJobIssue.class);
        when(postJobIssue.line()).thenReturn(line);
        when(postJobIssue.inputComponent()).thenReturn(inputPath);
        when(postJobIssue.severity()).thenReturn(severity);
        when(postJobIssue.ruleKey()).thenReturn(RuleKey.of("SQ-repo", rule));
        when(postJobIssue.message()).thenReturn(msg);
        when(postJobIssue.isNew()).thenReturn(isNew);
        return postJobIssue;
    }

    private InputPath createInputPath(String path, Boolean isFile) {
        File file = mock(File.class);
        when(file.isFile()).thenReturn(isFile);
        InputPath inputPath = mock(InputPath.class);
        when(inputPath.file()).thenReturn(file);
        when(inputPath.relativePath()).thenReturn(path);
        return inputPath;
    }
}
