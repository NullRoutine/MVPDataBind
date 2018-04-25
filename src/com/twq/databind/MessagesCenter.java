package com.twq.databind;

import com.intellij.openapi.ui.Messages;

public class MessagesCenter {
    public MessagesCenter() {
    }

    public static void showErrorMessage(String context, String title) {
        Messages.showMessageDialog(context, title, Messages.getErrorIcon());
    }

    public static void showMessage(String context, String title) {
        Messages.showMessageDialog(context, title, Messages.getInformationIcon());
    }

    public static void showDebugMessage(String context, String title) {
    }
}
