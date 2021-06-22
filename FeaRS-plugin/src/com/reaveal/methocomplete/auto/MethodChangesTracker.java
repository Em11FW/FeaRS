package com.reaveal.methocomplete.auto;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.openapi.vfs.newvfs.BulkFileListener;
import com.intellij.openapi.vfs.newvfs.events.VFileEvent;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.source.PsiClassImpl;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.messages.MessageBusConnection;
import com.reaveal.methocomplete.Constants;
import com.reaveal.methocomplete.OddsAndEnds;
import com.reaveal.methocomplete.PsiMethodUtil;
import com.reaveal.methocomplete.ServerCommunicator;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MethodChangesTracker implements BulkFileListener {

    private boolean m_isActive;
    private int m_currentThreshold;
    private Project m_project = null;

    private ToolWindow toolWindow = null; //TODO put it as local method variable. Does plugin crashes?
//    private static SettingPanel sw = null;
    private static MainWindow mw = null;

    private MessageBusConnection connection; //NEW from https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003028820-BulkFileListener-behavior-with-out-of-process-files //TODO replace this method for deprecated `addVirtualFileListener()`
    private VirtualFileListener changeListener;
    private Map<PsiClass, List<PsiMethod>> existingMethods_perFile;
    private boolean DEBUGGING = false;

    private static MethodChangesTracker instance = null;

    void Start() {
        System.out.println("Starting MethodChangeTracker");
        m_isActive = true;
        existingMethods_perFile = RecordAllExistingMethods();
        if(existingMethods_perFile.size()==0)
        {
            OddsAndEnds.showInfoBalloon("Methocomplete Plugin", "No Java file found.");
        }
        VirtualFileManager.getInstance().addVirtualFileListener(changeListener);
        connection = ApplicationManager.getApplication().getMessageBus().connect(); //NEW from https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003028820-BulkFileListener-behavior-with-out-of-process-files
        connection.subscribe(VirtualFileManager.VFS_CHANGES, this); //NEW from https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003028820-BulkFileListener-behavior-with-out-of-process-files


//        sw.UpdateStartStopButtonText();
    }

    void Stop() {
        System.out.println("Stopping MethodChangeTracker");
        m_isActive = false;
        existingMethods_perFile.clear();
        VirtualFileManager.getInstance().removeVirtualFileListener(changeListener);

        //sw.UpdateStartStopButtonText();
    }


    @Override
    public void after(@NotNull List<? extends VFileEvent> events) { //NEW from https://intellij-support.jetbrains.com/hc/en-us/community/posts/360003028820-BulkFileListener-behavior-with-out-of-process-files
        for (VFileEvent event : events) {
            VirtualFile eventFile = event.getFile();
            if (eventFile != null && !eventFile.isDirectory()) {
//                System.out.printf("[MODERN FILE CHANGE TRACKING ... test] : "+eventFile.toString());
            }
        }
    }


    private MethodChangesTracker() {
        m_currentThreshold = 2;
        System.out.println("Initializing MethodChangeTracker (only once)");

        changeListener = new VirtualFileListener() {
            @Override
            public void contentsChanged(@NotNull VirtualFileEvent event) {
                // This method will be called for each individual changed file
                VirtualFile changedFile = event.getFile();
                PsiFile psiFile = PsiManager.getInstance(m_project).findFile(changedFile);
                PsiClass changedClass = PsiTreeUtil.findChildOfType(psiFile, PsiClass.class);
                if (changedClass != null) {
                    List<PsiMethod> methods_before = existingMethods_perFile.get(changedClass);
                    if(methods_before == null) {
                        methods_before = new ArrayList<>();
//                        OddsAndEnds.showInfoBalloon("Warning","No method is recorded for this class: "+changedFile.toString());
//                        return;
                    }
                    List<PsiMethod> methods_now = ((PsiClassImpl) changedClass).getOwnMethods();

                    Set s_methods_before = new HashSet(methods_before);
                    Set s_methods_now = new HashSet(methods_now);

                    Set<PsiMethod> s_newMethods = new HashSet(s_methods_now);
                    s_newMethods.removeAll(s_methods_before);

                    if (s_newMethods.size() > 0) {
                        OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME,  changedClass.getName()+": "+s_newMethods.size()+" new method"+(s_newMethods.size()>1?"s":"")+" found. Sending to server...");
                        // New methods are added
                        ProcessNewlyAddedMethods(s_newMethods);
                    } else {
                        OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME, changedClass.getName()+": No new methods to send.");
                    }
                }
            }
        };
    }

    public void InitializeToolWindow(Project project)
    {
        m_project = project;


        /********** Method 1/2: Single tab ***********/
        if (toolWindow == null) {
            mw = new MainWindow(instance.m_project);
            toolWindow = ToolWindowManager.getInstance(instance.m_project).registerToolWindow(Constants.PLUGIN_NAME, mw,ToolWindowAnchor.RIGHT);
                    //registerToolWindow(Constants.PLUGIN_NAME, false/*Can close tabs?*/, ToolWindowAnchor.RIGHT);
        }
        toolWindow.setAutoHide(false);

//        /******** Method 2/2: Multiple tab ***********/
//        if (toolWindow == null) {
//
//            toolWindow = ToolWindowManager.getInstance(instance.m_project).registerToolWindow(Constants.PLUGIN_NAME, false/*Can close tabs?*/, ToolWindowAnchor.RIGHT);
//            toolWindow.setAutoHide(false);
//
//            MainWindow mainWindow = new MainWindow(instance.m_project);
//            Content tw_main = ContentFactory.SERVICE.getInstance().createContent(mainWindow, "Suggestions", false);
//            toolWindow.getContentManager().addContent(tw_main);
//
//            SettingPanel settings = new SettingPanel(instance.m_project);
//            Content tw_settings = ContentFactory.SERVICE.getInstance().createContent(settings, "Settings", false);
//            toolWindow.getContentManager().addContent(tw_settings);
//
//            mw = mainWindow;
//            sw = settings;
//        }
    }

    public static MethodChangesTracker GetInstance() {
        if (instance == null) {
            instance = new MethodChangesTracker();
        }
        return instance;
    }


    public void ProcessNewlyAddedMethods(Set<PsiMethod> s_newMethods) {
        if (DEBUGGING) {
//            String selectedMethods_debug = "";
//            for (PsiMethod m : s_newMethods)
//                selectedMethods_debug += m.getContainingClass().getName() + "::" + m.getName() + "\n";
//
//            String finalSelectedMethods_debug = selectedMethods_debug;
//            ApplicationManager.getApplication().invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    Messages.showInfoMessage(m_project, finalSelectedMethods_debug, "Result");
//                }
//            });

            List<ServerResult> results = ProcessResult(SERVER_MOCK_TWO_RESPONSE);
            mw.UpdateUI(results);
        } else {
            System.out.println("Creating CompletableFuture!");

            Set<String> s_newMethods_strings = new HashSet<>();
            for(PsiMethod aMethod: s_newMethods) // Why converting to String for ther rest of code flow? because we should read from PSI object from the same thread
                s_newMethods_strings.add(aMethod.getText());

            CompletableFuture<Void> completableFuture = CompletableFuture
                    .supplyAsync(() -> ServerCommunicator.SendNewMethodsToServerAndRetrieveSuggestions(s_newMethods_strings, m_currentThreshold))
                    .thenAccept((server_response_json) -> {
                        List<ServerResult> results = ProcessResult(server_response_json);
                        ApplicationManager.getApplication().invokeLater(() -> mw.UpdateUI(results));
                    } );
//            String server_response_json = ServerCommunicator.SendNewMethodsToServerAndRetrieveSuggestions(s_newMethods, m_currentThreshold);

//            while (!completableFuture.isDone()) {
//                System.out.println("CompletableFuture is not finished yet...");
//            }

//            String server_response_json = null;
//            try {
//                server_response_json = completableFuture.get();
//            } catch (Exception e) {
//                OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME,"Failed to process results!");
//                e.printStackTrace();
//            }


//            ApplicationManager.getApplication().invokeLater(new Runnable() {
//                        @Override
//                        public void run() {
//                            Messages.showInfoMessage(m_project, server_response_json, "Server Response:");
//                        }
//                    });

//            List<ServerResult> results = ProcessResult(server_response_json);



        }
    }

    private List<ServerResult> ProcessResult(String server_response_json) {

        List<ServerResult> recommendationGroups = null;

        try {
            JSONObject obj = new JSONObject(server_response_json);

            if (obj == null) {
                OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME,
                        "Unexpected server response (" +
                                server_response_json.length() + " chars): " + server_response_json);
            } else if (obj.getString("error").isEmpty() == false) {
                OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME,
                        "Error. Server says (" +
                                obj.getString("error").length() + " chars): " + obj.getString("error"));
            } else {
                // Valid response
                recommendationGroups = new ArrayList<>();
                JSONArray results_json_array = obj.getJSONArray("results");
                for (int i = 0; i < results_json_array.length(); i++)
                    recommendationGroups.add(new ServerResult((JSONObject) results_json_array.get(i)));
            }
        } catch (JSONException e) {
            OddsAndEnds.showInfoBalloon(Constants.PLUGIN_NAME,
                    "Unexpected server response (" +
                            server_response_json.length() + " chars): " + server_response_json);
            e.printStackTrace();
        }

        return recommendationGroups;
    }


    private Map<PsiClass, List<PsiMethod>> RecordAllExistingMethods() {
        System.out.println("MethodChangeTracker: Recording All Methods");
        List<PsiClass> classes = PsiMethodUtil.GetAllPsiClasses(m_project);
        Map<PsiClass, List<PsiMethod>> res = new HashMap<>();
        for (PsiClass c : classes) {
            List<PsiMethod> methods = PsiMethodUtil.GetAllOwnPsiMethods(c);
            res.put(c, methods);
        }
        return res;
    }

    boolean IsActive() {
        return m_isActive && (m_project != null);
    }

    public int GetCurrentThreshold() {
        return m_currentThreshold;
    }

    public void SetCurrentThreshold(int currentThreshold) {
        this.m_currentThreshold = currentThreshold;
    }

    public Project GetProject()
    {
        return m_project;
    }


    final String SERVER_MOCK_ONE_RESPONSE = "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"based_on\": [\n" +
            "        \"public void updateLocation()\",\n" +
            "        \"public void startLocationUpdates()\"\n" +
            "      ],\n" +
            "      \"suggestions\": [\n" +
            "        {source:\"https://www.google.com/search?q=Code1\",code:\"public String getLastKnownLocation() {\\n\\tGPSTracker gpsTracker = new GPSTracker();\\n\\n\\tif (gpsTracker.canGetLocation()) {\\n\\t\\tlatitude = String.valueOf(gpsTracker.latitude);\\n\\t\\tlongitude = String.valueOf(gpsTracker.longitude);\\n\\t}\\n\\n\\treturn latitude + \\\",\\\" + longitude;\\n}\"}," +
            "        {source:\"https://www.google.com/search?q=Code2\",code:\"package com.reaveal.methocomplete;\\n\\nimport com.intellij.notification.Notification;\\nimport com.intellij.notification.NotificationType;\\nimport com.intellij.notification.Notifications;\\nimport com.intellij.openapi.application.ApplicationManager;\\n\\npublic class OddsAndEnds\\n{\\n    static public void showInfoBalloon(String title, String content)\\n    {\\n        ApplicationManager.getApplication().invokeLater(new Runnable()\\n        {\\n            @Override\\n            public void run()\\n            {\\n                Notification notif = new Notification(\\\"Methocomplete_Plugin\\\", title, content, NotificationType.INFORMATION);\\n                Notifications.Bus.notify(notif);\\n            }\\n        });\\n    }\\n}\\n\\n\"},\n" +
            "        {source:\"https://www.google.com/search?q=Code3\",code:\"package com.reaveal.methocomplete;\"}\n" +
            "      ]\n" +
            "    },\n" +
            "  ],\n" +
            "  \"error\": \"\"\n" +
            "}";
    final String SERVER_MOCK_TWO_RESPONSE = "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"based_on\": [\n" +
            "        \"void m1(int a)\",\n" +
            "        \"int m2()\"\n" +
            "      ],\n" +
            "      \"suggestions\": [\n" +
            "        {source:\"https://www.google.com/search?q=Code1\",code:\"import com.intellij.psi.PsiFile;\\n public class MethodChangesTracker {\\n    int currentThreshold = -1; // range [1, 4]\\n    private boolean m_isActive;\\n};\"},\n" +
            "        {source:\"https://www.google.com/search?q=Code2\",code:\"package com.reaveal.methocomplete;\\n\\nimport com.intellij.notification.Notification;\\nimport com.intellij.notification.NotificationType;\\nimport com.intellij.notification.Notifications;\\nimport com.intellij.openapi.application.ApplicationManager;\\n\\npublic class OddsAndEnds\\n{\\n    static public void showInfoBalloon(String title, String content)\\n    {\\n        ApplicationManager.getApplication().invokeLater(new Runnable()\\n        {\\n            @Override\\n            public void run()\\n            {\\n                Notification notif = new Notification(\\\"Methocomplete_Plugin\\\", title, content, NotificationType.INFORMATION);\\n                Notifications.Bus.notify(notif);\\n            }\\n        });\\n    }\\n}\\n\\n\"},\n" +
            "        {source:\"https://www.google.com/search?q=Code3\",code:\"package com.reaveal.methocomplete;\\n\\nimport javax.swing.*;\\nimport javax.swing.event.ChangeEvent;\\nimport javax.swing.event.ChangeListener;\\nimport java.awt.*;\\nimport java.awt.event.ActionEvent;\\nimport java.util.Hashtable;\\n\\npublic class SettingsUI extends JPanel {\\n\\n    private JSlider m_thresholdSlider;\\n    JButton m_sliderConfirmBtn;\\n\\n    public SettingsUI() {\\n        super();\\n\\n        this.setLayout(new BoxLayout(this , BoxLayout.X_AXIS));\\n        InitializeSlider();\\n        InitializeOKBtn();\\n    }\\n\\n    private void InitializeSlider()\\n    {\\n        JLabel lbl = new JLabel(\\\"Proactiveness:\\\");\\n        this.add(lbl);\\n\\n        m_thresholdSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, MethodChangesTracker.GetInstance().currentThreshold);\\n        m_thresholdSlider.setSnapToTicks(true);\\n        m_thresholdSlider.setMajorTickSpacing(1);\\n        m_thresholdSlider.setPreferredSize(new Dimension(200,40));\\n        m_thresholdSlider.setPaintTicks(true);\\n        Hashtable<Integer, JLabel> labels =  new Hashtable<Integer, JLabel>();\\n        labels.put(1, new JLabel(\\\"Low\\\"));\\n        labels.put(2, new JLabel(\\\"Medium\\\"));\\n        labels.put(3, new JLabel(\\\"High\\\"));\\n        labels.put(4, new JLabel(\\\"Intense\\\"));\\n        m_thresholdSlider.setLabelTable(labels);\\n        m_thresholdSlider.setPaintLabels(true);\\n        m_thresholdSlider.addChangeListener(new ChangeListener()\\n        {\\n            @Override\\n            public void stateChanged(ChangeEvent e)\\n            {\\n                JSlider source = (JSlider)e.getSource();\\n                if (!source.getValueIsAdjusting()) {\\n                    int value = (int)source.getValue();\\n                    MethodChangesTracker.GetInstance().currentThreshold = value;\\n                    System.out.println(value);\\n                }\\n            }\\n        });\\n\\n        this.add(m_thresholdSlider);\\n    }\\n\\n    private void InitializeOKBtn()\\n    {\\n        m_sliderConfirmBtn = new JButton(\\\"Start Tracking\\\");\\n        m_sliderConfirmBtn.addActionListener(new AbstractAction()\\n        {\\n            @Override\\n            public void actionPerformed(ActionEvent e)\\n            {\\n                System.out.println(\\\"We will override this action in the class which use this class\\\");\\n            }\\n        });\\n        this.add(m_sliderConfirmBtn);\\n    }\\n}\"}\n" +
            "      ]\n" +
            "    },\n" +
            "    {\n" +
            "      \"based_on\": [\n" +
            "        \"void m3(int a)\",\n" +
            "        \"int m4()\"\n" +
            "      ],\n" +
            "      \"suggestions\": [\n" +
            "        {source:\"https://www.google.com/search?q=Code1\",code:\"package com.reaveal.methocomplete;\\n\\nimport com.intellij.notification.Notification;\\nimport com.intellij.notification.NotificationType;\\nimport com.intellij.notification.Notifications;\\nimport com.intellij.openapi.application.ApplicationManager;\\n\\npublic class OddsAndEnds\\n{\\n    static public void showInfoBalloon(String title, String content)\\n    {\\n        ApplicationManager.getApplication().invokeLater(new Runnable()\\n        {\\n            @Override\\n            public void run()\\n            {\\n                Notification notif = new Notification(\\\"Methocomplete_Plugin\\\", title, content, NotificationType.INFORMATION);\\n                Notifications.Bus.notify(notif);\\n            }\\n        });\\n    }\\n}\\n\\n\"},\n" +
            "        {source:\"https://www.google.com/search?q=Code2\",code:\"package com.reaveal.methocomplete;\\n\\nimport javax.swing.*;\\nimport javax.swing.event.ChangeEvent;\\nimport javax.swing.event.ChangeListener;\\nimport java.awt.*;\\nimport java.awt.event.ActionEvent;\\nimport java.util.Hashtable;\\n\\npublic class SettingsUI extends JPanel {\\n\\n    private JSlider m_thresholdSlider;\\n    JButton m_sliderConfirmBtn;\\n\\n    public SettingsUI() {\\n        super();\\n\\n        this.setLayout(new BoxLayout(this , BoxLayout.X_AXIS));\\n        InitializeSlider();\\n        InitializeOKBtn();\\n    }\\n\\n    private void InitializeSlider()\\n    {\\n        JLabel lbl = new JLabel(\\\"Proactiveness:\\\");\\n        this.add(lbl);\\n\\n        m_thresholdSlider = new JSlider(JSlider.HORIZONTAL, 1, 4, MethodChangesTracker.GetInstance().currentThreshold);\\n        m_thresholdSlider.setSnapToTicks(true);\\n        m_thresholdSlider.setMajorTickSpacing(1);\\n        m_thresholdSlider.setPreferredSize(new Dimension(200,40));\\n        m_thresholdSlider.setPaintTicks(true);\\n        Hashtable<Integer, JLabel> labels =  new Hashtable<Integer, JLabel>();\\n        labels.put(1, new JLabel(\\\"Low\\\"));\\n        labels.put(2, new JLabel(\\\"Medium\\\"));\\n        labels.put(3, new JLabel(\\\"High\\\"));\\n        labels.put(4, new JLabel(\\\"Intense\\\"));\\n        m_thresholdSlider.setLabelTable(labels);\\n        m_thresholdSlider.setPaintLabels(true);\\n        m_thresholdSlider.addChangeListener(new ChangeListener()\\n        {\\n            @Override\\n            public void stateChanged(ChangeEvent e)\\n            {\\n                JSlider source = (JSlider)e.getSource();\\n                if (!source.getValueIsAdjusting()) {\\n                    int value = (int)source.getValue();\\n                    MethodChangesTracker.GetInstance().currentThreshold = value;\\n                    System.out.println(value);\\n                }\\n            }\\n        });\\n\\n        this.add(m_thresholdSlider);\\n    }\\n\\n    private void InitializeOKBtn()\\n    {\\n        m_sliderConfirmBtn = new JButton(\\\"Start Tracking\\\");\\n        m_sliderConfirmBtn.addActionListener(new AbstractAction()\\n        {\\n            @Override\\n            public void actionPerformed(ActionEvent e)\\n            {\\n                System.out.println(\\\"We will override this action in the class which use this class\\\");\\n            }\\n        });\\n        this.add(m_sliderConfirmBtn);\\n    }\\n}\"},\n" +
            "        {source:\"https://www.google.com/search?q=Code3\",code:\"import com.intellij.psi.PsiFile;\\n public class MethodChangesTracker {\\n    int currentThreshold = -1; // range [1, 4]\\n    private boolean m_isActive;\\n};\"}\n" +
            "      ]\n" +
            "    }\n" +
            "  ],\n" +
            "  \"error\": \"\"\n" +
            "}";
}
