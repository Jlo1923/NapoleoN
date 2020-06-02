package com.naposystems.pepito.utility

import android.database.Cursor
import androidx.recyclerview.widget.RecyclerView


abstract class BaseCursorAdapter<V : RecyclerView.ViewHolder?>(c: Cursor?) :
    RecyclerView.Adapter<V>() {
    private var mCursor: Cursor? = null
    private var mDataValid = false
    private var mRowIDColumn = 0

    init {
        swapCursor(c)
    }

    abstract fun onBindViewHolder(holder: V, cursor: Cursor?)

    override fun onBindViewHolder(holder: V, position: Int) {
        check(mDataValid) { "Cannot bind view holder when cursor is in invalid state." }
        mCursor?.let { cursor ->
            check(cursor.moveToPosition(position)) { "Could not move cursor to position $position when trying to bind view holder" }
        }
        onBindViewHolder(holder, mCursor)
    }

    override fun getItemCount(): Int {
        return if (mDataValid) {
            mCursor?.count ?: 0
        } else {
            0
        }
    }

    override fun getItemId(position: Int): Long {
        check(mDataValid) { "Cannot lookup item id when cursor is in invalid state." }
        mCursor?.let { cursor ->
            check(cursor.moveToPosition(position)) { "Could not move cursor to position $position when trying to get an item id" }
        }
        return mCursor?.getLong(mRowIDColumn) ?: -1
    }

    fun getItem(position: Int): Cursor? {
        check(mDataValid) { "Cannot lookup item id when cursor is in invalid state." }
        mCursor?.let { cursor ->
            check(cursor.moveToPosition(position)) { "Could not move cursor to position $position when trying to get an item id" }
        }
        return mCursor
    }

    fun swapCursor(newCursor: Cursor?) {
        if (newCursor === mCursor) {
            return
        }
        if (newCursor != null) {
            mCursor = newCursor
            mDataValid = true
            // notify the observers about the new cursor
            notifyDataSetChanged()
        } else {
            notifyItemRangeRemoved(0, itemCount)
            mCursor = null
            mRowIDColumn = -1
            mDataValid = false
        }
    }
}