package com.chikeandroid.debtmanager20.iowe.loader;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.AsyncTaskLoader;

import com.chikeandroid.debtmanager20.data.Debt;
import com.chikeandroid.debtmanager20.data.PersonDebt;
import com.chikeandroid.debtmanager20.data.source.PersonDebtsRepository;
import com.chikeandroid.debtmanager20.util.EspressoIdlingResource;

import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Chike on 5/2/2017.
 */

public class IOweLoader extends AsyncTaskLoader<List<PersonDebt>> implements PersonDebtsRepository.DebtsRepositoryObserver  {

    private final PersonDebtsRepository mDebtsRepository;

    public IOweLoader(Context context, @NonNull PersonDebtsRepository repository) {
        super(context);
        checkNotNull(repository);
        mDebtsRepository = repository;
    }

    @Override
    public List<PersonDebt> loadInBackground() {

        // App is busy until further notice
        EspressoIdlingResource.increment();

        return mDebtsRepository.getAllPersonDebtsByType(Debt.DEBT_TYPE_IOWE);
    }

    @Override
    public void deliverResult(List<PersonDebt> data) {
        if(isReset()) {
            return;
        }

        if(isStarted()) {
            super.deliverResult(data);
        }
    }

    @Override
    protected void onStartLoading() {
        // Deliver any previously loaded data immediately if available.
        if(mDebtsRepository.cachedIOweDebtsAvailable()) {
            deliverResult(mDebtsRepository.getAllPersonDebtsByType(Debt.DEBT_TYPE_IOWE));
        }

        // Begin monitoring the underlying data source
        mDebtsRepository.addContentObserver(this);

        if(takeContentChanged() || !mDebtsRepository.cachedIOweDebtsAvailable()) {
            // When a change has  been delivered or the repository cache isn't available, we force
            // a load.
            forceLoad();
        }
    }

    @Override
    public void onDebtsChanged(int debtType) {
        if(debtType == Debt.DEBT_TYPE_OWED) {
            if (isStarted()) {
                forceLoad();
            }
        }
    }

    @Override
    protected void onStopLoading() {
        cancelLoad();
    }

    @Override
    protected void onReset() {
        onStopLoading();
        mDebtsRepository.removeContentObserver(this);
    }
}