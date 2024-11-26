package ch.cyberduck.binding.foundation;/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

/*
 * Copyright (c) 2002-2022 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.rococoa.ObjCClass;
import org.rococoa.Rococoa;

public abstract class NSProgress extends NSObject {
    private static final _Class CLASS = Rococoa.createClass("NSProgress", _Class.class);

    public interface _Class extends ObjCClass {
        NSProgress alloc();

        NSProgress progressWithTotalUnitCount(long count);

        NSProgress discreteProgressWithTotalUnitCount(long count);

        NSProgress currentProgress();
    }

    public static NSProgress progressWithParent(NSProgress parent, NSDictionary userInfo) {
        return CLASS.alloc().initWithParent_userInfo(parent, userInfo);
    }

    /**
     * Creates and returns an NSProgress instance, initialized using initWithParent:userInfo:.
     * <p>
     * The initializer is passed the current progress object, if there is one, and the value of the totalUnitCount property is set.
     * <p>
     * In many cases you can simply precede code that does a substantial amount of work with an invocation of this method, then repeatedly set the completedUnitCount or cancelled property in the loop that does the work.
     * <p>
     * You can invoke this method on one thread and then message the returned NSProgress on another thread. For example, you can capture the created progress instance in a block that you pass to dispatch_async. In that block you can invoke methods like becomeCurrentWithPendingUnitCount: or resignCurrent, and set the completedUnitCount or cancelled properties as work is carried out.
     *
     * @param unitCount The total number of units of work to be carried out.
     * @return Creates and returns an NSProgress instance
     */
    public static NSProgress progressWithTotalUnitCount(long unitCount) {
        return CLASS.progressWithTotalUnitCount(unitCount);
    }

    /**
     * Creates and returns an NSProgress instance with the specified totalUnitCount that is not part of any existing
     * progress tree. The instance is initialized using initWithParent:userInfo: with the parent set to nil.
     * <p>
     * Use this method to create the top level progress object returned by your own custom classes. The user of the returned progress object can add it to a progress tree using addChild:withPendingUnitCount:.
     * <p>
     * You are responsible for updating the progress count of the created progress object. You can invoke this method on one thread and then message the returned NSProgress on another thread. For example, you can capture the created progress instance in a block that you pass to dispatch_async. In that block you can invoke methods like becomeCurrentWithPendingUnitCount: or resignCurrent, and set the completedUnitCount or cancelled properties as work is carried out.
     *
     * @param unitCount The total number of units of work to be carried out.
     * @return Creates and returns an NSProgress instance
     */
    public static NSProgress discreteProgressWithTotalUnitCount(long unitCount) {
        return CLASS.discreteProgressWithTotalUnitCount(unitCount);
    }

    /**
     * Returns the NSProgress instance, if any, associated with the current thread by a previous invocation of becomeCurrentWithPendingUnitCount:.
     * <p>
     * Use this per-thread currentProgress value to allow code that performs work to report useful progress even when it
     * is widely separated from the code that actually presents progress information to the user, without requiring layers
     * of intervening code to pass around an NSProgress instance.
     * <p>
     * When reporting progress, you typically work with a child progress object, created by calling
     * discreteProgressWithTotalUnitCount:, to ensure that you report progress in known units of work.
     *
     * @return The NSProgress instance associated with the current thread, if any.
     */
    public static NSProgress currentProgress() {
        return CLASS.currentProgress();
    }

    /**
     * Initializes a newly allocated NSProgress instance.
     *
     * @param parent   The parent NSProgress object, if any, to notify when reporting progress or to consult
     *                 when checking for cancellation.
     *                 <p>
     *                 The only valid values are [NSProgress currentProgress] or nil.
     * @param userInfo The user information dictionary for the progress object. May be nil.
     * @return This is the designated initializer for the NSProgress class.
     */
    public abstract NSProgress initWithParent_userInfo(NSProgress parent, NSDictionary userInfo);

    /**
     * This property identifies the kind of progress being made, such as NSProgressKindFile. It can be nil.
     * <p>
     * If the value of the localizedDescription property has not previously been set to a non-nil value, the
     * default localizedDescription getter uses the progress kind to determine how to use the values of other
     * properties, as well as values in the user info dictionary, to create a string that is presentable to the user.
     *
     * @param kind A string identifying the kind of progress being made.
     */
    public abstract void setKind(String kind);

    /**
     * Balance the most recent previous invocation of becomeCurrentWithPendingUnitCount: on the same thread by
     * restoring the current progress object to what it was before becomeCurrentWithPendingUnitCount: was invoked.
     */
    public abstract void resignCurrent();

    /**
     * By default, NSProgress objects are not pausable.
     * <p>
     * You typically use this property to communicate whether controls for pausing should appear in a progress
     * reporting user interface. NSProgress itself does not do anything with this property other than help pass
     * the value from progress reporters to progress observers.
     * <p>
     * If an NSProgress is pausable, you should implement the ability to pause either by setting a block for the
     * pausingHandler property, or by polling the paused property periodically while performing the relevant work.
     * <p>
     * It is valid for the value of this property to change during the lifetime of an NSProgress object. By default,
     * NSProgress is KVO-compliant for this property, sending notifications on the same thread that updates the property.
     *
     * @param value Indicates whether the receiver is tracking work that can be paused.
     */
    public abstract void setPausable(boolean value);

    /**
     * <p>
     * By default, NSProgress objects are cancellable.
     * <p>
     * You typically use this property to communicate whether controls for canceling should appear in a progress reporting
     * user interface. NSProgress itself does not do anything with this property other than help pass the value from
     * progress reporters to progress observers.
     * <p>
     * If an NSProgress is cancellable, you should implement the ability to cancel progress either by setting a block
     * or the cancellationHandler property, or by polling the cancelled property periodically while performing the relevant work.
     * <p>
     * It is valid for the value of this property to change during the lifetime of an NSProgress object. By default,
     * NSProgress is KVO-compliant for this property, sending notifications on the same thread that updates the property.
     *
     * @param value Indicates whether the receiver is tracking work that can be cancelled.
     */
    public abstract void setCancellable(boolean value);

    /**
     * For an NSProgress with a kind of NSProgressKindFile, the unit of this property is bytes while the NSProgressFileTotalCountKey
     * and NSProgressFileCompletedCountKey keys in the userInfo dictionary are used for the overall count of files.
     * <p>
     * For any other kind of NSProgress, the unit of measurement does not matter as long as it is consistent. The values
     * may be reported to the user in the localizedDescription and localizedAdditionalDescription.
     *
     * @param completedUnitCount The number of units of work for the current job that have already been completed.
     */
    public abstract void setCompletedUnitCount(long completedUnitCount);

    /**
     * For an NSProgress with a kind of NSProgressKindFile, the unit of this property is bytes while the
     * NSProgressFileTotalCountKey and NSProgressFileCompletedCountKey keys in the userInfo dictionary are used for the overall count of files.
     *
     * @param totalUnitCount The total number of units of work tracked for the current progress.
     */
    public abstract void setTotalUnitCount(long totalUnitCount);

    /**
     * For an NSProgress with a kind of NSProgressKindFile, the unit of this property is bytes, and the NSProgressFileTotalCountKey and
     * NSProgressFileCompletedCountKey keys in the userInfo dictionary report the overall count of files.
     * <p>
     * For any other kind of NSProgress, the unit of measurement doesn’t matter as long as it’s consistent. You can report the values to
     * the user in the localizedDescription and localizedAdditionalDescription.
     *
     * @return The total number of tracked units of work for the current progress.
     */
    public abstract long totalUnitCount();

    /**
     * For an NSProgress with a kind of NSProgressKindFile, the unit of this property is bytes, and the NSProgressFileTotalCountKey and
     * NSProgressFileCompletedCountKey keys in the userInfo dictionary report the overall count of files.
     * <p>
     * For any other kind of NSProgress, the unit of measurement doesn’t matter as long as it’s consistent. You can report the values to
     * the user in the localizedDescription and localizedAdditionalDescription.
     *
     * @return The number of completed units of work for the current job.
     */
    public abstract long completedUnitCount();

    /**
     * If the current progress is operating on a set of files, set this property to the total number of files in the operation.
     * <p>
     * If present, NSProgress presents additional information in its localized description by setting a value in the userInfo dictionary.
     *
     * @return The total number of files for a file progress object.
     */
    public abstract NSNumber fileTotalCount();

    /**
     * If the current progress is operating on a set of files, set this property to the number of completed files in the operation.
     * <p>
     * If present, NSProgress presents additional information in its localized description by setting a value in the userInfo dictionary.
     *
     * @return The number of completed files for a file progress object.
     */
    public abstract NSNumber fileCompletedCount();

    /**
     * A KVO-compliant dictionary that changes in response to setUserInfoObject:forKey:. The dictionary sends all of
     * its KVO notifications on the thread that updates the property.
     * <p>
     * Some entries have meanings that the NSProgress class recognizes. For more information, see Recognizing Kinds of Progress, Using General Keys, Using
     * File Operation Keys, and Recognizing Kinds of File Operations.
     *
     * @return A dictionary of arbitrary values for the receiver.
     */
    public abstract NSDictionary userInfo();

    /**
     * Publishes the progress object for other processes to observe it.
     */
    public abstract void publish();

    /**
     * Removes a progress object from publication, making it unobservable by other processes.
     */
    public abstract void unpublish();

    /**
     * Sets the receiver as the current progress object of the current thread and specifies the portion of work to be
     * performed by the next child progress object of the receiver.
     *
     * @param unitCount The number of units of work to be carried out by the next progress object that is initialized by
     *                  invoking the initWithParent:userInfo: method in the current thread with the receiver set as the parent.
     *                  This number represents the portion of work to be performed in relation to the total number of units
     *                  of work to be performed by the receiver (represented by the value of the receiver’s totalUnitCount
     *                  property). The units of work represented by this parameter must be the same units of work that are
     *                  used in the receiver’s totalUnitCount property.
     */
    public abstract void becomeCurrentWithPendingUnitCount(long unitCount);

    /**
     * Sets a value in the user info dictionary.
     *
     * @param objectOrNil The object to set for the specified key, or nil to remove an existing entry in the dictionary.
     * @param key         The key for storing the specified object.
     */
    public abstract void setUserInfoObject_forKey(NSObject objectOrNil, String key);

    /**
     * Set this value when the kind property is NSProgressKindFile to describe the kind of file operation.
     *
     * @param kind The kind of file operation for the progress object.
     * @since macOS 10.13+
     */
    public abstract void setFileOperationKind(String kind);

    /**
     * Set this value for a progress that you publish to subscribers that register for updates using addSubscriberForFileURL:withPublishingHandler:.
     *
     * @param url A URL that represents the file for the current progress object.
     * @since macOS 10.13+
     */
    public abstract void setFileURL(NSURL url);

    /**
     * If present, NSProgress presents additional information in its localized description by setting a value in the userInfo dictionary.
     *
     * @param throughput A value that represents the speed of data processing, in bytes per second.
     * @since macOS 10.13+
     */
    public abstract void setThroughput(NSNumber throughput);

    /**
     * The value that indicates that the progress is tracking a file operation.
     * If you set this value for the progress kind, set a value in the user info dictionary for the NSProgressFileOperationKindKey.
     * @since macOS 10.9+
     */
    public static final String NSProgressKindFile = "NSProgressKindFile";
    /**
     * A key with a corresponding value that indicates the kind of file operation a progress object represents.
     * @since macOS 10.9+
     */
    public static final String NSProgressFileOperationKindKey = "NSProgressFileOperationKindKey";
    /**
     * A key with a corresponding value that represents the file URL of a file operation for the progress object.
     *
     * @since macOS 10.9+
     */
    public static final String NSProgressFileURLKey = "NSProgressFileURLKey";
    /**
     * The progress is tracking a file upload operation.
     * @since macOS 10.9+
     */
    public static final String NSProgressFileOperationKindUploading = "NSProgressFileOperationKindUploading";
    /**
     * The progress is tracking a file download operation.
     *
     * @since macOS 10.9+
     */
    public static final String NSProgressFileOperationKindDownloading = "NSProgressFileOperationKindDownloading";
    /**
     * The progress is tracking file decompression after a download.
     *
     * @since macOS 10.9+
     */
    public static final String NSProgressFileOperationKindDecompressingAfterDownloading = "NSProgressFileOperationKindDecompressingAfterDownloading";
    /**
     * A key with a corresponding value that represents the time remaining, in seconds.
     * @since macOS 10.9+
     */
    public static final String NSProgressEstimatedTimeRemainingKey = "NSProgressEstimatedTimeRemainingKey";
    /**
     * A key with a corresponding value that indicates the speed of data processing, in bytes per second.
     * @since macOS 10.9+
     */
    public static final String NSProgressThroughputKey = "NSProgressThroughputKey";
}
