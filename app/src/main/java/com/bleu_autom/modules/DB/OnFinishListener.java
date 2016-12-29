package com.bleu_autom.modules.DB;

import android.support.annotation.Nullable;

public interface OnFinishListener {
    void OnReceived(boolean success, String message, @Nullable Integer index);
}
