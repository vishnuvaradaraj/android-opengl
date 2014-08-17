package com.parabay.cinema.data;

import com.parabay.cinema.media.util.Future;
import com.parabay.cinema.media.util.FutureListener;

public interface DataSource {

    public void pause();

    public void resume();

    public Future<DataItem> nextItem(FutureListener<DataItem> listener);

}
