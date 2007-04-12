//
//  NSCalendarDateAdditions.h
//  Keychain
//
//  Created by Wade Tregaskis on 16/5/2005.
//
//  Copyright (c) 2005, Wade Tregaskis.  All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
//    * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
//    * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
//    * Neither the name of Wade Tregaskis nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

#import <Cocoa/Cocoa.h>


/*! @category NSCalendarDate (KeychainAdditions)
    @abstract Extends NSCalendarDate to support fractions of a second.
    @discussion This category adds methods to NSCalendarDate to support fractions of a second.  Note that this support does not extend to format strings (yet, at least), so methods such as description, descriptionWithCalendarFormat:, dateWithString:calendarFormat: etc will not benefit. */

@interface NSCalendarDate (KeychainFramework)

/*! @method dateWithYear:month:day:hour:minute:second:fractionalSecond:timeZone:
    @abstract Returns an NSCalendarDate with the given time.
    @discussion Works identically to NSCalendarDate's dateWithYear:month:day:hour:minute:second:timeZone: method, except for the extra sub-second precision.
    @param year The year for the date.
    @param month The month for the date.
    @param day The day for the date.
    @param hour The hour for the date.
    @param minute The minute for the date.
    @param second The second for the date.
    @param fractionalSecond The fractional second for the date.
    @param aTimeZone The time zone the date exists in.
    @result Returns an appropriate NSCalendarDate, or nil if an error occurs. */

+ (id)dateWithYear:(int)year month:(unsigned)month day:(unsigned)day hour:(unsigned)hour minute:(unsigned)minute second:(unsigned)second fractionalSecond:(double)fractionalSecond timeZone:(NSTimeZone*)aTimeZone;

/*! @method initWithYear:month:day:hour:minute:second:fractionalSecond:timeZone:
    @abstract Initialises the receiver with the given time values.
    @discussion Works identically to NSCalendarDate's initWithYear:month:day:hour:minute:second:fractionalSecond:timeZone:, except for the extra sub-second precision.
    @param year The year for the date.
    @param month The month for the date.
    @param day The day for the date.
    @param hour The hour for the date.
    @param minute The minute for the date.
    @param second The second for the date.
    @param fractionalSecond The fractional second for the date.
    @param aTimeZone The time zone the date exists in.
    @result Returns an appropriately initialised NSCalendarDate (which may not necessarily be the receiver), or nil if an error occurs (in which case the receiver is automatically released). */

- (id)initWithYear:(int)year month:(unsigned)month day:(unsigned)day hour:(unsigned)hour minute:(unsigned)minute second:(unsigned)second fractionalSecond:(double)fractionalSecond timeZone:(NSTimeZone*)aTimeZone;

/*! @method fractionalSecond
    @abstract Returns the fractional second part of the date.
    @discussion Returns any fractional second part of the date, as a number equal to or greater than 0.0 and less than 1.0.
    @result Returns the fractional seconds part of the receiver. */

- (double)fractionalSecond;

/*! @method dateByAddingYears:months:days:hours:minutes:seconds:fractionalSeconds:
    @abstract Returns a new date by adding the given time intervals to the receiver.
    @discussion Works identically to NSCalendarDate's dateByAddingYears:months:days:hours:minutes:seconds: method, except it adds on any fractionalSeconds component you supply.
    @param year Number of years to add (or subtract, if negative).
    @param month Number of months to add (or subtract, if negative).
    @param days Number of days to add (or subtract, if negative).
    @param hours Number of hours to add (or subtract, if negative).
    @param minutes Number of minutes to add (or subtract, if negative).
    @param seconds Number of seconds to add (or subtract, if negative).
    @param fractionalSeconds Number of fractional seconds to add (or subtract, if negative).
    @result Returns the resulting NSCalendarDate, or nil if an error occurs. */

- (NSCalendarDate*)dateByAddingYears:(int)year months:(int)month days:(int)day hours:(int)hour minutes:(int)minute seconds:(int)second fractionalSeconds:(double)fractionalSecond;

/*! @method years:months:days:hours:minutes:seconds:fractionalSeconds:sinceDate:
    @abstract Returns the difference between the receiver and a given reference date, broken down into years, months, etc.
    @discussion Works exactly the same as NSCalendarDate's years:months:days:hours:minutes:seconds:sinceDate: method, except for the extra sub-second precision.  See the relevant documentation for details. */

- (void)years:(int*)yearsPointer months:(int*)monthsPointer days:(int*)daysPointer hours:(int*)hoursPointer minutes:(int*)minutesPointer seconds:(int*)secondsPointer fractionalSeconds:(double*)fractionalSecondsPointer sinceDate:(NSCalendarDate*)date;

/*! @method classicMacLongDateTime
    @abstract Returns a Classic MacOS-style LongDateTime value (SInt64), expressed as seconds since 1/1/1904 (see DateTimeUtils.h).
    @discussion This is only necessary because Keychain searches can only specify creation and modification dates in this form, due to an implementation oversight in the Security framework.  In future the Security framework will hopefully support more common date formats, but for now this function must remain to fill that gap.
    @result Returns the receiver expressed as seconds since January 1st, 1904 in Classic MacOS-style LongDateTime format (SInt64). */

- (int64_t)classicMacLongDateTime;

/*! @method classicMacDateTime
    @abstract Returns a Classic MacOS-style DateTime value (UInt32), expressed as seconds since 1/1/1904 (see DateTimeUtils.h).
    @discussion This essentially just works the same as classicMacDateTime, except it can only represent dates from the reference date (January 1st, 1904) up to February 6th, 2040.  If you try to convert dates outside of this range an exception will be raised.
    @result Returns the receiver expressed as seconds since January 1st, 1904 in Classic MacOS-style DateTime format (UInt32). */

- (uint32_t)classicMacDateTime;

@end
