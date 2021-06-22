package com.reaveal.methocomplete;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.application.ApplicationManager;

public class OddsAndEnds
{
    static public void showInfoBalloon(String title, String content)
    {
        ApplicationManager.getApplication().invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                Notification notif = new Notification("Methocomplete_Plugin", title, content, NotificationType.INFORMATION);
                Notifications.Bus.notify(notif);
            }
        });
    }
}

