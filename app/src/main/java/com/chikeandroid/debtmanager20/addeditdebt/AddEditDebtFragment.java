package com.chikeandroid.debtmanager20.addeditdebt;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.chikeandroid.debtmanager20.R;
import com.chikeandroid.debtmanager20.data.Debt;
import com.chikeandroid.debtmanager20.data.Person;
import com.chikeandroid.debtmanager20.data.PersonDebt;
import com.chikeandroid.debtmanager20.databinding.FragmentAddDebtBinding;
import com.chikeandroid.debtmanager20.util.TimeUtil;
import com.chikeandroid.debtmanager20.util.ValidationUtil;
import com.chikeandroid.debtmanager20.util.validator.EditTextFullNameValidator;
import com.chikeandroid.debtmanager20.util.validator.EditTextIntegerValidator;
import com.chikeandroid.debtmanager20.util.validator.EditTextPhoneNumberValidator;

import java.util.Calendar;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Chike on 3/16/2017.
 */

public class AddEditDebtFragment extends Fragment implements AddEditDebtContract.View {

    public static final String ARGUMENT_EDIT_DEBT = "com.chikeandroid.debtmanager20.debtdetail.DebtDetailFragment.EDIT_DEBT";

    private EditText mEditTextAmount;
    private EditText mEditTextName;
    private EditText mEditTextPhoneNumber;
    private EditText mEditTextComment;
    private Button mButtonDateDue;
    private Button mButtonDateCreated;
    private int mDebtType;
    private static final int REQUEST_CONTACT = 1;
    private Calendar mCalendar;
    private long mDebtCreatedAt;
    private long mDebtDue;
    private PersonDebt mPersonDebt;

    private AddEditDebtContract.Presenter mPresenter;
    private FragmentAddDebtBinding mFragmentAddDebtBinding;
    private String mActionBarTitle;

    public static AddEditDebtFragment newInstance(Bundle bundle) {
        AddEditDebtFragment addEditDebtFragment = new AddEditDebtFragment();
        addEditDebtFragment.setArguments(bundle);
        return addEditDebtFragment;
    }

    public AddEditDebtFragment() {

    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        mFragmentAddDebtBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_add_debt, container, false);

        setUpViews();

        mActionBarTitle = "Add Debt";
        setViewsTextFromBundle();

        setUpToolbar();

        setHasOptionsMenu(true);
        setRetainInstance(true);

        return mFragmentAddDebtBinding.getRoot();
    }

    private void setUpViews() {
        mCalendar = Calendar.getInstance();

        mDebtCreatedAt = System.currentTimeMillis();
        mDebtDue = System.currentTimeMillis();

        mButtonDateCreated = mFragmentAddDebtBinding.btnDateCreated;
        mButtonDateCreated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(mButtonDateCreated.getId(), mDebtCreatedAt);
            }
        });

        mEditTextComment = mFragmentAddDebtBinding.etComment;
        mEditTextAmount = mFragmentAddDebtBinding.etAmount;
        mEditTextName = mFragmentAddDebtBinding.etFullName;
        mEditTextPhoneNumber = mFragmentAddDebtBinding.etPhoneNumber;
        mButtonDateDue = mFragmentAddDebtBinding.btnDateDue;
        mButtonDateDue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(mButtonDateDue.getId(), mDebtDue);
            }
        });

        String currentDateString = TimeUtil.millis2String(System.currentTimeMillis(), "MMM d, yyyy");

        mButtonDateCreated.setText(String.format(getString(R.string.created_date), currentDateString));
        mButtonDateDue.setText(String.format(getString(R.string.due_date), currentDateString));

        RadioGroup radioGroupDebtType = mFragmentAddDebtBinding.rgDebtType;
        mDebtType = Debt.DEBT_TYPE_OWED;
        radioGroupDebtType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int checkedId) {

                if(checkedId == R.id.rb_owed_by_me) {
                    mDebtType = Debt.DEBT_TYPE_IOWE;
                }else if(checkedId == R.id.rb_owed_to_me) {
                    mDebtType = Debt.DEBT_TYPE_OWED;
                }
            }
        });

        ImageButton imageButtonContacts = mFragmentAddDebtBinding.ibContacts;
        imageButtonContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pickContactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                startActivityForResult(pickContactIntent, REQUEST_CONTACT);
            }
        });
    }

    private void setViewsTextFromBundle() {

        Bundle bundle = getArguments();
        if(bundle != null) {
            mPersonDebt = bundle.getParcelable(ARGUMENT_EDIT_DEBT);
            checkNotNull(mPersonDebt);
            mDebtDue = mPersonDebt.getDebt().getDueDate();
            mDebtCreatedAt = mPersonDebt.getDebt().getCreatedDate();
            mEditTextName.setText(mPersonDebt.getPerson().getFullname());
            mEditTextPhoneNumber.setText(mPersonDebt.getPerson().getPhoneNumber());
            mEditTextAmount.setText(String.valueOf(mPersonDebt.getDebt().getAmount()));
            mEditTextComment.setText(mPersonDebt.getDebt().getNote());
            String dueDateString = String.format(getString(R.string.due_date),
                    TimeUtil.millis2String(mPersonDebt.getDebt().getDueDate(), "MMM d, yyyy"));
            mButtonDateDue.setText(dueDateString);
            String createdDateString = String.format(getString(R.string.created_date),
                    TimeUtil.millis2String(mPersonDebt.getDebt().getCreatedDate(), "MMM d, yyyy"));
            mButtonDateCreated.setText(createdDateString);
            mActionBarTitle = "Edit Debt";

            if(mPersonDebt.getDebt().getDebtType() == Debt.DEBT_TYPE_OWED) {
                mFragmentAddDebtBinding.rbOwedToMe.setChecked(true);
                mFragmentAddDebtBinding.rbOwedByMe.setChecked(false);
            }else if(mPersonDebt.getDebt().getDebtType() == Debt.DEBT_TYPE_IOWE) {
                mFragmentAddDebtBinding.rbOwedToMe.setChecked(false);
                mFragmentAddDebtBinding.rbOwedByMe.setChecked(true);
            }
        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = mFragmentAddDebtBinding.toolbar;
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(mActionBarTitle);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_add_debt, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                getActivity().finish();
                break;
            case R.id.action_save_debt:
                if(ValidationUtil.isInValid(new EditTextFullNameValidator(mEditTextName, getActivity()),
                        new EditTextPhoneNumberValidator(mEditTextPhoneNumber, getActivity()),
                        new EditTextIntegerValidator(mEditTextAmount, getActivity())
                )) {

                    Toast.makeText(getActivity(), "Invalid", Toast.LENGTH_LONG).show();

                }else {

                    String personId = UUID.randomUUID().toString();
                    String debtId = UUID.randomUUID().toString();
                    // update
                    if(mPersonDebt != null) {
                        personId = mPersonDebt.getPerson().getId();
                        debtId = mPersonDebt.getDebt().getId();
                    }

                    Person person = new Person(personId, mEditTextName.getText().toString(),
                            mEditTextPhoneNumber.getText().toString());

                    Debt debt = new Debt.Builder(debtId, person.getId(),
                            Double.valueOf(mEditTextAmount.getText().toString()), mDebtCreatedAt,
                            mDebtType, Debt.DEBT_STATUS_ACTIVE)
                            .dueDate(mDebtDue)
                            .note(mEditTextComment.getText().toString())
                            .build();
                    mPresenter.saveDebt(person, debt);
                }
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }

    @Override
    public void showErroSavingDebt() {

    }

    @Override
    public void showDebts() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }

    @Override
    public void showEmptyDebtError() {
        Toast.makeText(getActivity(), getString(R.string.msg_empty_debt), Toast.LENGTH_LONG).show();
    }


    @Override
    public void setPresenter(AddEditDebtContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != Activity.RESULT_OK) {
            return;
        }
        if(requestCode == REQUEST_CONTACT) {
            Uri contactUri = data.getData();
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER
            };
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
            if(c.getCount() == 0) {
                c.close();
                return;
            }
            c.moveToFirst();
            mEditTextName.setText(c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)));
            mEditTextPhoneNumber.setText(c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)));
            c.close();
        }
    }

    private void showDatePickerDialog(final int buttonId, long dateTimeStamp) {

        mCalendar.setTimeInMillis(dateTimeStamp);
        int year = mCalendar.get(Calendar.YEAR);
        int month = mCalendar.get(Calendar.MONTH);
        int dayOfMonth = mCalendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String dateString = TimeUtil.millis2String(mCalendar.getTimeInMillis(), "MMM d, yyyy");

                if(buttonId == mButtonDateDue.getId()) {
                    mButtonDateDue.setText(String.format(getString(R.string.due_date), dateString));
                    mDebtDue = mCalendar.getTimeInMillis();

                }else if(buttonId == mButtonDateCreated.getId()) {
                    mButtonDateCreated.setText(String.format(getString(R.string.created_date), dateString));
                    mDebtCreatedAt = mCalendar.getTimeInMillis();
                }

            }
        }, year, month, dayOfMonth);

        datePickerDialog.show();
    }
}