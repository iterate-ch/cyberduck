/*
** Copyright 1998, 1999, 2001, 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.util;

import java.util.Calendar;
import java.util.Date;


// --- Revision History ---
// 13Feb99 GLG  create
// 23Feb99 GLG  add factory-method to make Date's from macTime's
// 23Feb99 GLG  edit doc-comments
// 01Apr99 GLG  expand doc-comments
// 06Apr99 GLG  add getGMTOffsetMinutes() as convenience
// 14Apr99 GLG  change package
// 23Apr99 GLG  change package
// 02Jun99 GLG  change package yet again
// 01Jun01 GLG  rework to have fewer args and options (cut needless complexity)
// 22Jun01 GLG  add methods that to-and-fro with Mac OS X's UTCDateTime
// 23Jun01 GLG  add toMillis and toFracs constants for clarity
// 25Jun01 GLG  add doc-comments about local-time and time-stamp conversion ambiguities
// 26Jun01 GLG  rework macSecsToJavaMillis() and javaMillisToMacSecs() to handle DST better
// 26Jun01 GLG  give old methods new names: localSecsToJavaMillis(), javaMillisToLocalSecs()
// 15Feb2003 GLG  refactor to eliminate toFracs


/**
** The MacTime class contains
** static methods to convert between Java's time-values and Mac OS time-values.
** The Java time values are in milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
** Classical Mac OS time values are in seconds after 01 Jan 1904 midnight local-time.
**<p>
** Mac OS UTCDateTime time values are measured in 48.16 fixed-point
** seconds after 01 Jan 1904 midnight UTC/GMT.
** The 48.16 fixed-point form is passed in a long,
** so the low 16 bits represent 65536ths of a second.
**<p>
** This class is pure Java and contains no Mac-platform dependencies, other than the
** obvious knowledge of how Mac OS represents time/date values.
** The conversions involving local Mac OS time values are approximate, and rely
** on the default TimeZone.  The conversions are correct except near the changeover
** points between standard time and DST (if observed).
**<p>
** Classical Mac OS stores the time-stamps on files in local time, not GMT.
** This gives rise to ambiguities when converting between stored local times and
** Java-format GMT milliseconds.  The problem is that a host machine cannot know
** what time-zone or DST-convention was in effect at the time represented by
** the stored local time, so a guess must be made.  Different guessing algorithms have
** different results, and also different kinds of errors and magnitudes.
**
** <h3>Guessing a Time-Zone</h3>
**
** The simplest guessing algorithm is to just use the current host's current settings,
** regardless of the time represented in the time-stamp itself.
** This is a pretty good guess for non-mobile machines in non-DST-observing time-zones.
** Unfortunately, most of the U.S.A. observes DST (I happen to live in a non-DST zone).
** It's a worse guess when the local time-zone convention observes DST, because the DST
** setting currently in effect may not be the one in effect at the time represented by
** the time-stamp.  In fact, it'll be wrong for half the year.
** It's an awful guess when the host machine is mobile and travels between time-zones regularly.
** It's a worthless guess when the time-stamp was generated on a machine in an
** unknown time-zone and DST-observing locale, and you simply receive a file with
** time-stamps encoded.  That is, when the files are mobile and the hosts may or
** may not also be mobile.
**<p>
** This simple guessing algorithm is embodied in
** localSecsToJavaMillis() and javaMillisToLocalSecs().
** This is the historical algorithm used in earlier MacBinary Toolkit versions.
**
** <h3>Guessing a Better Time-Zone</h3>
**
** A better guessing algorithm is to use the current host's TimeZone to adjust local times
** into GMT, taking into account the DST effect at the time of the time-stamp itself.
**  A TimeZone embodies a raw offset from GMT, along with rules that define
** an annual period when DST is in effect (or defining no DST effect at all).
** This algorithm works much better when the host is non-mobile.  It works equally well
** regardless of whether DST is observed at that location or not.
** It's an improvement for mobile hosts, too, but is still far from perfect.  You'll get fluctuations
** in the conversion from local time to GMT, depending on when and where the host is located,
** and what the stored time-stamps are.
** This makes stored local time time-stamps a bit of a gamble, because they may appear to have
** different GMT values
** simply because the time-zone is changing.  This cannot be helped, and is the cost of having
** a guessing algorithm that works harder to adjust for TimeZone DST effects.
**<p>
** Using a TimeZone seems to be an all-around better algorithm, but it's not without some
** drawbacks and difficulties in its implementation.
** First, it's still a guess, and it suffers larger or smaller errors depending on how far the current
** TimeZone is from the actual TimeZone that was in effect at the time of a local time-stamp.
** This is identical to the simpler guessing algorithm, so don't think you've avoided it
** by using a smarter algorithm.
** Second, the conversion from local time taking
** DST into account must first know whether DST was in effect at the time of the time-stamp.
** This presents a conundrum when converting local times near the DST changeover points.
** For example, does a local time of 2:15 am on the day of DST cessation represent the time
** one hour after 1:15 am?
** Or does it represent the time two hours after 1:15 am, when "wall-clock time" is going
** through the 2-o'clock hour for the second time?
** Or consider a local time-stamp that represents
** 2:30 am on the day of DST commencement.  Obviously, this can't happen in a time-zone
** where DST was observed, but how should the conversion handle this "impossible" time?
** It can appear on files received from hosts in TimeZones that don't observe DST,
** so is not really "impossible".
** Impossible in one TimeZone isn't always impossible in another.
**<p>
** There is also a conundrum when dealing with the Calendar and TimeZone classes themselves.
** While we'd like to use TimeZone.getOffset() to determine the net effective GMT offset
** including DST, we first have to calculate an era, year, month, day, day of week, and 
** milliseconds since midnight.  But we can only do that using a Calendar.  But a Calendar
** needs a TimeZone to calculate those fields.  But we can't use the default TimeZone, since
** its DST rules will affect what Calendar calculates for the fields.  So either we can't use
** a Calendar at all, or we have to use a Calendar with some special TimeZone.
** Help me-e-e-e, Mister Wiza-a-a-ard!!
**<p>
** Rather than attempt to resolve these conundrums, the guessing algorithm just makes
** an approximation.  This approximation is actually exact everywhere and everywhen 
** <b>EXCEPT near the DST changeover points</b> (commencement and cessation).
** Near those points, the algorithm returns consistent values (same outputs for identical
** inputs), but the exact results depend on how Calendar and TimeZone are implemented.
** Furthermore, if the TimeZone does not observe DST, then the approximation is always exact.
** This is logical, since a TimeZone that doesn't observe DST has no DST changeover points.
**<p>
** The algorithm is explained in more detail at
** macSecsToJavaMillis() and javaMillisToMacSecs().
** These methods are called by the classical Mac OS FileForker imps to
** convert between internal classical Mac OS time-stamps and Java milliseconds.
** These methods are also used in MacBinaryHeader, to convert the time-stamps
** stored in the MacBinary header.
**
** <h3>Guessing How Time-Stamps are Stored</h3>
**
** To further complicate the time-zone guessing game, different Mac OS volume-formats
** store time-stamps differently.  HFS always and only stores local time representations.
** HFS+, however, stores GMT representations.  Thus, an HFS+ volume can theoretically
** avoid all ambiguities about the time-stamps on files.  I say "theoretically" because
** avoiding the ambiguity also depends on which File Manager API is used to access
** which file-system.  The older FSSpec-based API accessing an HFS+ volume will return
** local-time time-stamps which have been calculated from the underlying GMT time-stamps.
** But if you encode the file as MacBinary, transfer it to another machine in a different time-zone
** or DST-convention, then you've lost information about the original GMT basis for the time-stamp.
** Or if you MacBinary-encode a file and decode it on the same mobile machine that's in a different 
** time-zone than where it was when the encoding occurred, and a similar thing can happen.
**<p>
** Things get really fun when code has to start guessing whether a file is on an HFS volume or
** an HFS+ volume.  Or maybe it's on a non-Mac volume format, such as the Microsoft FAT
** volume formats (local-time time-stamps, as I recall), or the UFS format (GMT time-stamps).
** Or maybe it's on a remote machine and we have no idea how it stores time-stamps.
** Guess which format and whether the OS does a conversion before returning the time-stamp.
** Guess again whether the conversion out of local time will encounter an edge anomaly
** because the time-stamp is near a DST changeover point.
** Guess whether repeated round-trip conversions will be pathological or not.
** Guess whether there's a better guessing algorithm at all.
**<p>
** The possibilities of guessing HFS or HFS+, which API returned which time-format, etc.
** are beyond all reasonable hope.  None of those things are accounted for in any method here.
**
** <h3>Guessing Time-Zones and MacBinary</h3>
**
** Encoding a file into MacBinary encodes the file's time-stamps in local time, not GMT,
** which is what the MacBinary standard requires.  Anyone decoding the file will then treat
** the time-stamps as being in their local time-zone, and convert them to GMT time-stamps.
** This can cause problems when time-stamps must be compared across time-zones,
** or between dates when DST is in effect vs. when it's not.
**<p>
** Programs doing backups and restores using MacBinary encoding must be aware of this,
** along with how time-stamps are stored on specific volumes backed up (to account for
** the GMT vs. local differences, such as in HFS+ vs. HFS).
**
** <h3>Guessing in Mac OS X</h3>
**
** Despite the guess-infested picture painted above,
** the Mac OS X FileForker implementation does not suffer any
** time-zone or DST lapses when representing or returning time-stamps on HFS+ files.
** This is because it uses the FSRef-based APIs, which use UTCDateTime
** time-stamp forms.  Thus, the conversion to Java milliseconds never has to adjust anything
** based on the current host's time-zone and DST settings.
** You can see this in the methods 
** macUTCDateTimeToJavaMillis() and javaMillisToMacUTCDateTime(),
** which perform a straight arithmetic conversion without taking anything else into account.
**<p>
** At long last, something that doesn't have to be guessed.
** Whew.
**
** @author Gregory Guerin
*/

public class MacTime
{
	/**
	** MAC_TIME_DELTA is the number of milliseconds 
	** between the Mac OS epoch and Java epoch, i.e. the respective <i>t = 0 </i>points.
	** Mac OS time-keeping starts at 01 Jan 1904, and Java at 01 Jan 1970.
	** There are 24107 days between those dates, according to my HP calculator.
	** This value does not account for the fact that Mac-time is in the local time-zone,
	** while Java-time is in UTC/GMT.
	*/
	public static final long 
		MAC_TIME_DELTA = 24107L * 24L * 60L * 60L * 1000L;


	/**
	** A single Calendar used repeatedly by macSecsToJavaMillis() and javaMillisToMacSecs()
	** to convert from local time time-stamps into Java milliseconds.
	** Thread-safety is ensured by synchronizing on the Calendar itself as needed.
	*/
	private static Calendar localCal = Calendar.getInstance();


	/**
	** Convert a local Mac OS time in UNSIGNED seconds to a Java time 
	** measured as milliseconds before or after 01 Jan 1970 midnight GMT.
	** The local time represented by macTimeSecs
	** is always assumed to be in the current host's time-zone, 
	** and to observe DST according to the default TimeZone active when
	** this class was loaded.  That is, calls to TimeZone.setDefault() have no
	** subsequent effect on this class.
	**<p>
	** The effect of DST-observance is calculated at the represented macTimeSecs,
	** i.e. as if the default TimeZone were active at that time.
	** This method guesses the DST-effect on the conversion using an approximation.
	** The algorithm is as follows:
	**<ol>
	**  <li>Set the Calendar's time to a prospective GMT milliseconds value
	**     that takes time-zone offset from GMT into account but ignores DST.
	**  </li>
	**  <li>Retrieve the resulting Calendar's DST_OFFSET field.
	**  </li>
	**  <li>Subtract that DST_OFFSET field from the prospective milliseconds.
	**  </li>
	**  <li>Return the resulting value as the converted milliseconds.
	**  </li>
	**</ol>
	** This algorithm is always correct in a TimeZone that does not observe DST.
	** In TimeZones that observe DST,
	** this algorithm is correct everywhere and everywhen except near the DST
	** end point.  More precisely, it's correct except when the prospective
	** GMT milliseconds lies within the interval where wall-clock time is replaying
	** the prior o'clock hour, showing an ambiguous interval of wall-clock time.
	** We just feign blissful ignorance and pretend it all really works.
	*/
	public static long 
	macSecsToJavaMillis( int macTimeSecs )
	{
		// Calculate prospective GMT milliseconds without taking DST into account.
		// Not synchronized on localCal, since its assigned TimeZone never changes.
		long prospective = (0x0FFFFFFFFL & macTimeSecs) * 1000L
				- MAC_TIME_DELTA - localCal.getTimeZone().getRawOffset();

		synchronized ( localCal )
		{
			// Set the Calender to the prospective milliseconds, via an intermediate Date.
			// It's stupid that Calendar.setTimeInMillis() is protected, but there it is.
			// The resulting Calendar then gives us the DST_OFFSET field we need to
			// adjust the prospective time, yielding a DST-corrected result
			// (subject to the blissful ignorance inherent in this approximation).
			localCal.setTime( new Date( prospective ) );

			// Going from local time to GMT, subtract the DST offset.
			prospective -= localCal.get( Calendar.DST_OFFSET );
		}

		return ( prospective );
	}


	/**
	** Convert a Java time in milliseconds to a local Mac OS time
	** measured as seconds after 01 Jan 1904 midnight local-time.
	** The local time represented by the returned value
	** is always assumed to be in the current host's time-zone, 
	** and to observe DST according to the default TimeZone active when
	** this class was loaded.  That is, calls to TimeZone.setDefault() have no
	** subsequent effect on this class.
	**<p>
	** The effect of DST-observance is calculated at the represented javaTime,
	** i.e. as if the default TimeZone were active at that time.
	**<p>
	** Unlike macSecsToJavaMillis(), we don't have to guess what TimeZone to use.
	** Also, since the javaTime is in GMT, there are no discontinuities to worry about,
	** er, I mean blissfully ignore, near the DST changeover points.  Yes, the output value
	** has discontinuities at DST changeovers, but that's because it represents local time.
	*/
	public static long 
	javaMillisToMacSecs( long javaTime )
	{
		long offset = 0;

		synchronized ( localCal )
		{
			// Set the Calender to the given milliseconds, via an intermediate Date.
			// It's stupid that Calendar.setTimeInMillis() is protected, but there it is.
			// The resulting Calendar then gives us the DST_OFFSET and ZONE_OFFSET
			// fields we need to adjust the returned result.
			localCal.setTime( new Date( javaTime ) );
			offset = localCal.get( Calendar.ZONE_OFFSET ) + localCal.get( Calendar.DST_OFFSET );
		}

		// Add 500 mS before dividing by 1000, so time is rounded to nearest second.
		return ( ( javaTime + MAC_TIME_DELTA + offset + 500L) / 1000L );
	}



	/**
	** Convert a local Mac OS time in UNSIGNED seconds to a Java time 
	** measured as milliseconds before or after midnight 01 Jan 1970 GMT.
	** Since Mac OS time is local-time and Java is GMT, account for the offset
	** by calling localGMTOffsetMillis().  The current state of DST is used,
	** which is not necessarily the same as the state of DST in effect at
	** the represented macTimeSecs.
	*/
	public static long 
	localSecsToJavaMillis( int macTimeSecs )
	{
		return ( (0x0FFFFFFFFL & macTimeSecs) * 1000L
				- MAC_TIME_DELTA - localGMTOffsetMillis() );
	}

	/**
	** Convert a Java time in milliseconds to a local Mac OS time
	** measured as seconds after midnight 01 Jan 1904 local-time.
	** Since Mac OS time is local-time and Java is GMT, account for the offset
	** by calling localGMTOffsetMillis().  The current state of DST is used,
	** which is not necessarily the same as the state of DST in effect at
	** the represented javaTime.
	*/
	public static long 
	javaMillisToLocalSecs( long javaTime )
	{
		// Add 500 mS before dividing by 1000, so time is rounded to nearest second.
		return ( ( javaTime + MAC_TIME_DELTA + localGMTOffsetMillis() + 500L) / 1000L );
	}

	/**
	** Return the current host's current offset from GMT, measured in milliseconds,
	** which is negative for locations west of prime meridian,
	** positive for locations east.  That is, it's the GMT-offset of local-time, in milliseconds.
	**<p>
	** The current state of DST is taken into account,
	** which may not be the same as the state of daylight savings time in effect
	** at some past time.
	** Uses a new Calendar instance each time this method is called.
	** Can't cache a Calendar, because the offsets may have changed since prior call.
	*/
	public static long
	localGMTOffsetMillis()
	{
		Calendar when = Calendar.getInstance();
		return ( when.get( Calendar.ZONE_OFFSET ) + when.get( Calendar.DST_OFFSET ) );
	}


	/**
	** This conversion-factor is exact in binary floating-point.
	** Used in macUTCDateTimeToJavaMillis() and in javaMillisToMacUTCDateTime().
	*/
	private static final double toMillis = 1000.0D / 65536.0D;


	/**
	** Convert a Mac OS UTCDateTime in a long to a
	** Java time  measured as milliseconds before or after midnight 01 Jan 1970 GMT.
	** This conversion uses double-precision multiply and long/double conversions,
	** but does not use a Calendar or TimeZone.
	*/
	public static long 
	macUTCDateTimeToJavaMillis( long utcDateTime )
	{  return ( (long) ( toMillis * utcDateTime ) - MAC_TIME_DELTA );  }

	/**
	** Convert a Java time in milliseconds to a Mac OS UTCDateTime.
	** This conversion uses double-precision divide and long/double conversions,
	** but does not use a Calendar or TimeZone.
	*/
	public static long 
	javaMillisToMacUTCDateTime( long javaTime )
	{  return ( (long) ( (javaTime + MAC_TIME_DELTA) / toMillis ) );  }

}
