package com.stark.kmpcontact.android.contacts.data.local;

import android.util.Log;
import android.content.ContentValues;
import com.stark.kmpcontact.data.database.DatabaseException;
import com.stark.kmpcontact.data.database.DatabaseOperation;
import com.stark.kmpcontact.data.database.DatabaseRequestExecutor;
import com.stark.kmpcontact.data.local.model.ContactEntity;
import kotlinx.coroutines.Dispatchers;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010$\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0003\n\u0002\b\u0007\b\u0007\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J6\u0010\u0005\u001a\u0004\u0018\u00010\u00062\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0014\u0010\u000b\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\u00060\fH\u0096@\u00a2\u0006\u0002\u0010\rJ>\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0014\u0010\u000b\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\u00060\f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0013H\u0002J.\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0014\u0010\u000b\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\u00060\fH\u0002J@\u0010\u0015\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\n2\u0014\u0010\u000b\u001a\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\u00060\f2\u0006\u0010\u0010\u001a\u00020\u00112\b\u0010\u0016\u001a\u0004\u0018\u00010\u0006H\u0002J\u001a\u0010\u0017\u001a\u00020\b*\u0010\u0012\u0004\u0012\u00020\b\u0012\u0006\u0012\u0004\u0018\u00010\u00060\fH\u0002J\u000e\u0010\u0018\u001a\u00020\b*\u0004\u0018\u00010\u0006H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/stark/kmpcontact/android/contacts/data/local/SQLiteDatabaseRequestExecutor;", "Lcom/stark/kmpcontact/data/database/DatabaseRequestExecutor;", "contactsSQLiteOpenHelper", "Lcom/stark/kmpcontact/android/contacts/data/local/ContactsSQLiteOpenHelper;", "(Lcom/stark/kmpcontact/android/contacts/data/local/ContactsSQLiteOpenHelper;)V", "execute", "", "statement", "", "operation", "Lcom/stark/kmpcontact/data/database/DatabaseOperation;", "arguments", "", "(Ljava/lang/String;Lcom/stark/kmpcontact/data/database/DatabaseOperation;Ljava/util/Map;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logRequestFailure", "", "elapsedMs", "", "throwable", "", "logRequestStart", "logRequestSuccess", "result", "formatForLog", "formatResultForLog", "Companion", "contacts_debug"})
public final class SQLiteDatabaseRequestExecutor implements com.stark.kmpcontact.data.database.DatabaseRequestExecutor {
    @org.jetbrains.annotations.NotNull()
    private final com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper contactsSQLiteOpenHelper = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String LOG_TAG = "DatabaseExecutor";
    private static final int LOG_PREVIEW_LIMIT = 200;
    @org.jetbrains.annotations.NotNull()
    private static final com.stark.kmpcontact.android.contacts.data.local.SQLiteDatabaseRequestExecutor.Companion Companion = null;
    
    public SQLiteDatabaseRequestExecutor(@org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.android.contacts.data.local.ContactsSQLiteOpenHelper contactsSQLiteOpenHelper) {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public java.lang.Object execute(@org.jetbrains.annotations.NotNull()
    java.lang.String statement, @org.jetbrains.annotations.NotNull()
    com.stark.kmpcontact.data.database.DatabaseOperation operation, @org.jetbrains.annotations.NotNull()
    java.util.Map<java.lang.String, ? extends java.lang.Object> arguments, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<java.lang.Object> $completion) {
        return null;
    }
    
    private final void logRequestStart(java.lang.String statement, com.stark.kmpcontact.data.database.DatabaseOperation operation, java.util.Map<java.lang.String, ? extends java.lang.Object> arguments) {
    }
    
    private final void logRequestSuccess(java.lang.String statement, com.stark.kmpcontact.data.database.DatabaseOperation operation, java.util.Map<java.lang.String, ? extends java.lang.Object> arguments, long elapsedMs, java.lang.Object result) {
    }
    
    private final void logRequestFailure(java.lang.String statement, com.stark.kmpcontact.data.database.DatabaseOperation operation, java.util.Map<java.lang.String, ? extends java.lang.Object> arguments, long elapsedMs, java.lang.Throwable throwable) {
    }
    
    private final java.lang.String formatForLog(java.util.Map<java.lang.String, ? extends java.lang.Object> $this$formatForLog) {
        return null;
    }
    
    private final java.lang.String formatResultForLog(java.lang.Object $this$formatResultForLog) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/stark/kmpcontact/android/contacts/data/local/SQLiteDatabaseRequestExecutor$Companion;", "", "()V", "LOG_PREVIEW_LIMIT", "", "LOG_TAG", "", "contacts_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}