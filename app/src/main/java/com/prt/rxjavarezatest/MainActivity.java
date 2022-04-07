package com.prt.rxjavarezatest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Flow;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.observers.DisposableCompletableObserver;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.Callable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static String TAG = "REZA_TEST";

    CompositeDisposable compositeDisposable;

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        compositeDisposable = new CompositeDisposable();

        findViewById(R.id.completable_button).setOnClickListener(this);
        findViewById(R.id.single_button).setOnClickListener(this);
        findViewById(R.id.flowable_button).setOnClickListener(this);
        findViewById(R.id.cancel_button).setOnClickListener(this);

        textView = findViewById(R.id.text_view);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.completable_button) {
            startCompletable();
        } else if (v.getId() == R.id.single_button) {
            startSingle();
        } else if (v.getId() == R.id.flowable_button) {
            startFlowable();
        } else if (v.getId() == R.id.cancel_button) {
            cancel();
        }
    }

    private void startCompletable() {
        if (compositeDisposable != null) {
            if (compositeDisposable.isDisposed()) {
                compositeDisposable = new CompositeDisposable();
            }
        }
        Disposable completable = Completable.fromAction(new Action() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "onStart: Before delay");
                    Thread.sleep(3000);
                    Log.d(TAG, "onStart: " + Thread.currentThread().getName());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribeWith(new DisposableCompletableObserver() {
            @Override
            public void onComplete() {
                Log.d(TAG, "onComplete: " + Thread.currentThread().getName());
                textView.setText("Completed");
            }

            @Override
            public void onError(@NonNull Throwable e) {
                e.printStackTrace();
            }
        });

        compositeDisposable.add(completable);
    }

    private void startSingle() {
        if (compositeDisposable != null) {
            if (compositeDisposable.isDisposed()) {
                compositeDisposable = new CompositeDisposable();
            }
        }
        Disposable single = Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                Thread.sleep(3000);
                return 3;
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableSingleObserver<Integer>() {
                    @Override
                    public void onSuccess(@NonNull Integer integer) {
                        textView.setText(integer.toString());
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.d(TAG, "onError: " + e);
                    }
                });

        compositeDisposable.add(single);
    }

    private void startFlowable() {
        if (compositeDisposable != null) {
            if (compositeDisposable.isDisposed()) {
                compositeDisposable = new CompositeDisposable();
            }
        }
        Flowable<Integer> flowable = Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(@NonNull FlowableEmitter<Integer> emitter) {
                for (int i = 0; i < 10; i++) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (!emitter.isCancelled()) {
                        emitter.onNext(i + 1);
                    }
                }
                if (!emitter.isCancelled()) {
                    emitter.onComplete();
                }
            }
        }, BackpressureStrategy.LATEST)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        Disposable flowableDisposable = flowable.subscribe(new Consumer<Integer>() {
            @Override
            public void accept(Integer integer) throws Throwable {
                textView.setText(integer.toString());
            }
        });

        compositeDisposable.add(flowableDisposable);
    }

    private void cancel() {
        if (compositeDisposable != null) {
            compositeDisposable.dispose();
        }
    }
}