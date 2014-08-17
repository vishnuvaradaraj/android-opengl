package com.parabay.cinema.data;

import com.parabay.cinema.media.data.ContentListener;
import com.parabay.cinema.media.data.MediaItem;
import com.parabay.cinema.media.data.Path;

public interface DataSourceSequencer {
    public void addContentListener(ContentListener listener);
    public void removeContentListener(ContentListener listener);
    public long reload();
    public MediaItem getMediaItem(int index);
    public int findItemIndex(Path path, int hint);
}
