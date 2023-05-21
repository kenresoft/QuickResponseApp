package com.kixfobby.security.quickresponse.util;

import android.os.Environment;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Commons {

    public static final String MEDIA_DIR = Environment.getExternalStorageDirectory().getPath() + "/Kixfobby/QuickResponse/Media";

    @NotNull
    public static <T, W extends PickerItemWrapper<T>> List<W> wrapItems(@NotNull Collection<T> items, PickerItemWrapper.WrapperFactory<T, W> factory) {
        List<W> wrappedItems = new ArrayList<>();
        for (T item : items) {
            wrappedItems.add(factory.create(item));
        }
        return wrappedItems;
    }

    @Contract("null, _ -> null; !null, null -> null")
    public static <U, T extends PickerItemWrapper<U>> T findEqual(Collection<T> items, U target) {
        if (items == null || target == null) return null;
        for (T item : items) {
            if (item.get().equals(target)) {
                return item;
            }
        }
        return null;
    }
}
