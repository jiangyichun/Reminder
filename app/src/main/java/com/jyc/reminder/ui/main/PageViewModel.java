package com.jyc.reminder.ui.main;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import com.jyc.reminder.pojo.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PageViewModel extends ViewModel {

    private static List<Event> events = new ArrayList<>();

    private MutableLiveData<Integer> mIndex = new MutableLiveData<>();
    private LiveData<List<Event>> mEvents = Transformations.map(mIndex, new Function<Integer, List<Event>>() {
        @Override
        public List<Event> apply(Integer input) {
            return events.stream().filter(item -> {
                return input.intValue() == 1 ? !item.getOverdue() : item.getOverdue();
            }).collect(Collectors.toList());
        }
    });

    public void setIndex(int index) {
        mIndex.setValue(index);
    }

    public LiveData<List<Event>> getEvents() {
        return mEvents;
    }

    public static void setEvents(List<Event> events) {
        PageViewModel.events = events;
    }
}