package ch.cyberduck.binding.foundation;

/*
 * Copyright (c) 2002-2019 iterate GmbH. All rights reserved.
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
    private static final NSProgress._Class CLASS = Rococoa.createClass("NSProgress", NSProgress._Class.class);

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

    public abstract void publish();

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

    public abstract void setUserInfoObject_forKey(NSObject objectOrNil, String key);

    public static final String NSProgressKindFile = "NSProgressKindFile";
    public static final String NSProgressFileOperationKindKey = "NSProgressFileOperationKindKey";
    public static final String NSProgressFileDownloadingSourceURLKey = "NSProgressFileDownloadingSourceURLKey";
    public static final String NSProgressFileURLKey = "NSProgressFileURLKey";
}
