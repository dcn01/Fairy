/*
 * Copyright (C) 2017 Zane.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.zane.fairy.view.content;

import android.app.Application;
import android.arch.core.util.Function;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import me.zane.fairy.MySharedPre;
import me.zane.fairy.ZLog;
import me.zane.fairy.databinding.ActivityLogcatBinding;
import me.zane.fairy.repository.LogcatContentRepository;
import me.zane.fairy.vo.LogcatContent;

/**
 * Created by Zane on 2017/11/17.
 * Email: zanebot96@gmail.com
 */

public class LogcatContentViewModel extends AndroidViewModel{
    private final LogcatContentRepository repository;
    private int id;
    private ActivityLogcatBinding binding;

    public final ObservableField<String> filter = new ObservableField<>();
    public final ObservableField<String> options = new ObservableField<>();
    public final ObservableField<String> grep = new ObservableField<>();
    public final ObservableField<Boolean> isStartFetch = new ObservableField<>();

    private final MutableLiveData<String> isStartLiveData = new MutableLiveData<>();
    private LiveData<LogcatContent> contentLiveData;

    public LogcatContentViewModel(@NonNull Application application, LogcatContentRepository repository) {
        super(application);
        this.repository = repository;
    }

    //---------------------------------action binding---------------------------------
    void init(int id, String grep, ActivityLogcatBinding binding) {
        this.binding = binding;
        this.id = id;
        insertIfNotExits(new LogcatContent(id, "init fairy"));
        contentLiveData = Transformations.switchMap(isStartLiveData, grepData -> repository.getLogcatContent(id, grepData));
        repository.fetchFromData(id);
        isStartLiveData.setValue(grep);
    }

    public void onOptionsChanged(CharSequence s) {
        options.set(s.toString());
    }

    public void onFilterChanged(CharSequence s) {
        filter.set(s.toString());
    }

    public void onGrepChanged(CharSequence s) {
        grep.set(s.toString());
        //replace the data from repository (grep or not grep)
        isStartLiveData.setValue(s.toString());
    }

    public void onStartFetch() {
        isStartFetch.set(true);
        MySharedPre.getInstance().putIsStartFetch(id, true);
        repository.fetchData(options.get(), filter.get());
    }

    public void onStopFetch() {
        isStartFetch.set(false);
        MySharedPre.getInstance().putIsStartFetch(id, false);
        repository.stopFetch();
    }

    //---------------------------------toView------------------------------------
    boolean isStartFetch() {
        return MySharedPre.getInstance().getIsStartFetch(id);
    }

    void setStartFetch(boolean startFetch) {
        isStartFetch.set(startFetch);
        MySharedPre.getInstance().putIsStartFetch(id, startFetch);
    }

    LiveData<LogcatContent> getData() {
        return contentLiveData;
    }

    private void insertIfNotExits(LogcatContent content) {
        repository.insertIfNotExits(content);
    }

    void clearContent(LogcatContent content) {
        repository.clearContent(content);
    }

    /**
     * as same as onDestory(), but it won't be trigger when destory abnormal
     */
    @Override
    protected void onCleared() {
        repository.stopFetch();
        MySharedPre.getInstance().putIsStartFetch(id, false);
    }
}
