package com.kixfobby.security.quickresponse.helper

import android.annotation.SuppressLint
import android.content.*
import android.content.res.AssetFileDescriptor
import android.net.Uri
import android.os.Build
import android.util.Log
import com.kixfobby.security.quickresponse.R
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStreamReader


class ClipBoardManager {
    @SuppressLint("NewApi")
    fun copyToClipboard(context: Context, text: String?): Boolean {
        return try {
            val sdk = Build.VERSION.SDK_INT
            if (sdk < Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = context
                    .getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
                clipboard.text = text
            } else {
                val clipboard = context
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData
                    .newPlainText(
                        context.resources.getString(
                            R.string.copy_to_clipboard
                        ), text
                    )
                clipboard.setPrimaryClip(clip)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    @SuppressLint("NewApi")
    fun readFromClipboard(context: Context): String? {
        val sdk = Build.VERSION.SDK_INT
        if (sdk < Build.VERSION_CODES.HONEYCOMB) {
            val clipboard = context
                .getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
            return clipboard.text.toString()
        } else {
            val clipboard: ClipboardManager = context
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

            // Gets a content resolver instance
            val cr: ContentResolver = context.contentResolver

            // Gets the clipboard data from the clipboard
            val clip: ClipData? = clipboard.primaryClip
            if (clip != null) {
                var text: String? = null
                val title: String? = null

                // Gets the first item from the clipboard data
                val item = clip.getItemAt(0)

                // Tries to get the item's contents as a URI pointing to a note
                val uri: Uri = item.uri

                // If the contents of the clipboard wasn't a reference to a
                // note, then
                // this converts whatever it is to text.
                if (text == null) {
                    text = coerceToText(context, item).toString()
                }
                return text
            }
        }
        return ""
    }

    @SuppressLint("NewApi")
    fun coerceToText(context: Context, item: ClipData.Item): CharSequence {
        // If this Item has an explicit textual value, simply return that.
        val text = item.text
        if (text != null) {
            return text
        }

        // If this Item has a URI value, try using that.
        val uri: Uri? = item.uri
        if (uri != null) {

            // First see if the URI can be opened as a plain text stream
            // (of any sub-type). If so, this is the best textual
            // representation for it.
            var stream: FileInputStream? = null
            try {
                // Ask for a stream of the desired type.
                val descr: AssetFileDescriptor? = context.contentResolver
                    .openTypedAssetFileDescriptor(uri, "text/*", null)
                stream = descr?.createInputStream()
                val reader = InputStreamReader(
                    stream,
                    "UTF-8"
                )

                // Got it... copy the stream into a local string and return it.
                val builder = StringBuilder(128)
                val buffer = CharArray(8192)
                var len: Int
                while (reader.read(buffer).also { len = it } > 0) {
                    builder.append(buffer, 0, len)
                }
                return builder.toString()
            } catch (e: FileNotFoundException) {
                // Unable to open content URI as text... not really an
                // error, just something to ignore.
            } catch (e: IOException) {
                // Something bad has happened.
                Log.w("ClippedData", "Failure loading text", e)
                return e.toString()
            } finally {
                if (stream != null) {
                    try {
                        stream.close()
                    } catch (e: IOException) {
                    }
                }
            }

            // If we couldn't open the URI as a stream, then the URI itself
            // probably serves fairly well as a textual representation.
            return uri.toString()
        }

        // Finally, if all we have is an Intent, then we can just turn that
        // into text. Not the most user-friendly thing, but it's something.
        val intent = item.intent
        return if (intent != null) {
            intent.toUri(Intent.URI_INTENT_SCHEME)
        } else ""

        // Shouldn't get here, but just in case...
    }

}