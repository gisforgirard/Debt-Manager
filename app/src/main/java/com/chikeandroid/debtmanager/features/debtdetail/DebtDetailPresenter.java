package com.chikeandroid.debtmanager.features.debtdetail;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.chikeandroid.debtmanager.data.PersonDebt;
import com.chikeandroid.debtmanager.data.source.PersonDebtsDataSource;
import com.chikeandroid.debtmanager.data.source.PersonDebtsRepository;
import com.chikeandroid.debtmanager.data.loaders.DebtLoader;

import javax.inject.Inject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Chike on 4/20/2017.
 * Listens to user actions from the UI ({@link DebtDetailFragment}), retrieves the data and updates
 * the UI as required.
 */
public class DebtDetailPresenter implements DebtDetailContract.Presenter, LoaderManager.LoaderCallbacks<PersonDebt> {

    private static final int DEBT_QUERY = 106;

    @NonNull
    private final PersonDebtsDataSource mDebtsRepository;

    @NonNull
    private final DebtDetailContract.View mDebtDetailView;

    @NonNull
    private final LoaderManager mLoaderManager;

    private final DebtLoader mLoader;

    private final String mDebtId;

    @Inject
    public DebtDetailPresenter(@Nullable String debtId, PersonDebtsRepository debtsRepository,
                               DebtDetailContract.View view, LoaderManager loaderManager, DebtLoader loader) {
        mDebtId = debtId;
        mDebtsRepository = debtsRepository;
        mDebtDetailView = view;
        mLoaderManager = loaderManager;
        mLoader = loader;
    }

    @Inject
    void setUpListener() {
        mDebtDetailView.setPresenter(this);
    }

    @Override
    public void start() {
        mLoaderManager.initLoader(DEBT_QUERY, null, this);
    }

    @Override
    public void stop() {
        // stop presenter
    }

    @Override
    public void deletePersonDebt(@NonNull PersonDebt personDebt) {
        checkNotNull(personDebt);
        mDebtsRepository.deletePersonDebt(personDebt);
        mDebtDetailView.showPersonDebtDeleted();
    }

    @Override
    public void addAdditionalDebt() {
        // add additional debt
    }

    @Override
    public Loader<PersonDebt> onCreateLoader(int id, Bundle args) {
        if (mDebtId == null) {
            return null;
        }
        // can set loading indicator
        return mLoader;
    }

    @Override
    public void onLoadFinished(Loader<PersonDebt> loader, PersonDebt data) {
        if (data == null) {
            mDebtDetailView.showMissingDebt();
        }else {
            mDebtDetailView.showPersonDebt(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<PersonDebt> loader) {
        // remove any references it has to the Loader's data.
    }
}