package com.chikeandroid.debtmanager20.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.chikeandroid.debtmanager20.data.Debt;
import com.chikeandroid.debtmanager20.data.Person;
import com.chikeandroid.debtmanager20.data.source.DebtsDataSource;
import com.chikeandroid.debtmanager20.data.source.local.DebtsPersistenceContract.DebtsEntry;
import com.chikeandroid.debtmanager20.data.source.local.DebtsPersistenceContract.PersonsEntry;
import com.chikeandroid.debtmanager20.util.StringUtil;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Chike on 3/22/2017.
 */

public class DebtsLocalDataSource implements DebtsDataSource {

    private DebtsDbHelper mDebtsDbHelper;

    public DebtsLocalDataSource(@NonNull Context context) {
        checkNotNull(context);
        mDebtsDbHelper = new DebtsDbHelper(context);
    }

    @Override
    public void getDebts(@NonNull LoadDebtsCallback callback) {

    }

    @Override
    public void getDebt(@NonNull String debtId, @NonNull GetDebtCallback callback) {

    }

    @Override
    public void getIOwedDebts(@NonNull LoadDebtsCallback callback) {

    }

    @Override
    public void getOwedDebts(@NonNull LoadDebtsCallback callback) {

    }

    @Override
    public void saveDebt(@NonNull Debt debt, @NonNull Person person) {
        checkNotNull(debt);
        checkNotNull(person);

        String personId = person.getId();
        SQLiteDatabase db = mDebtsDbHelper.getWritableDatabase();

        if(!personAlreadyExist(person.getPhoneNumber())) {

            ContentValues personValues = new ContentValues();
            personValues.put(PersonsEntry.COLUMN_ENTRY_ID, person.getId());
            personValues.put(PersonsEntry.COLUMN_NAME, person.getFullname());
            personValues.put(PersonsEntry.COLUMN_PHONE_NO, person.getPhoneNumber());

            db.insert(PersonsEntry.TABLE_NAME, null, personValues);

        } else {

            personId = getPersonIdIfAlreadyExist(person.getPhoneNumber());

        }

        ContentValues debtValues = new ContentValues();
        debtValues.put(DebtsEntry.COLUMN_ENTRY_ID, debt.getId());
        debtValues.put(DebtsEntry.COLUMN_AMOUNT, debt.getAmount());
        debtValues.put(DebtsEntry.COLUMN_DATE_DUE, debt.getDueDate());
        debtValues.put(DebtsEntry.COLUMN_DATE_ENTERED, debt.getCreatedDate());
        debtValues.put(DebtsEntry.COLUMN_NOTE, debt.getNote());
        debtValues.put(DebtsEntry.COLUMN_PERSON_ID, personId);
        debtValues.put(DebtsEntry.COLUMN_STATUS, debt.getStatus());
        debtValues.put(DebtsEntry.COLUMN_TYPE, debt.getDebtType());

        db.insert(DebtsEntry.TABLE_NAME, null, debtValues);

        db.close();
    }

    @Override
    public void refreshDebts() {

    }

    @Override
    public void deleteAllDebts() {

    }

    @Override
    public void deleteDebt(@NonNull String debtId) {

    }

    private String getPersonIdIfAlreadyExist(String phoneNumber) {

        if(!StringUtil.isEmpty(phoneNumber)) {
            SQLiteDatabase db = mDebtsDbHelper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT entryid FROM " + PersonsEntry.TABLE_NAME +
                    " WHERE phone_no = ?", new String[]{String.valueOf(phoneNumber)});
            String id = "";
            if(cursor.moveToFirst()){
                id = cursor.getString(cursor.getColumnIndexOrThrow(PersonsEntry.COLUMN_ENTRY_ID));
            }
            cursor.close();
            return id;
        }
        return "";
    }

    private boolean personAlreadyExist(String phoneNumber) {

        if(!StringUtil.isEmpty(phoneNumber)) {
            SQLiteDatabase db = mDebtsDbHelper.getWritableDatabase();

            Cursor cursor = db.rawQuery("SELECT phone_no FROM " + PersonsEntry.TABLE_NAME +
                    " WHERE phone_no = ?", new String[]{String.valueOf(phoneNumber)});
            if(cursor.moveToFirst()){
                return true;
            }
            cursor.close();
        }
        return false;
    }
}
