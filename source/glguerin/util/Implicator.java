/*
** Copyright 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.util;


// --- Revision History ---
// 08May2003 GLG  create
// 09May2003 GLG  make Implicator.Chain be a nested class
// 09May2003 GLG  rework to support Implicator.Chain's Object-of-last-resort


/**
** An Implicator evalutes something about the current host platform or environment,
** and/or something about a provided Object, and returns an Object or null.
** Exactly what it evaluates and what it returns depends on the Implicator's implementation.
** That is, the implication depends on the implementation.
**<p>
** One meaning of the verb "implicate" is "to draw a connection between things".
** Simply stated, then, an Implicator draws a connection between something it
** evaluates and something it returns when the evaluation succeeds.
**<p>
** Implicator instancess are frequently arranged in a specific order as an Implicator.Chain.
** They may be independently evaluated, however, if that is desired.
**
** @author Gregory Guerin
*/
abstract public class Implicator
{
	/** Create. */
	protected
	Implicator()
	{  super();  }


	/**
	** Evaluate the environment and/or the given Object, which may be null.
	** If this implicates something, return a non-null Object.
	** If this doesn't implicate anything, return null.
	*/
	abstract public Object
	evaluate( Object given );



	/**
	** An Implicator.Chain is a chain of other Implicators.
	** You can add Implicators one at a time, or remove them all.
	** You can't remove them individually.
	**<p>
	** When you evaluate() an Implicator.Chain, it evaluate()'s each
	** Implicator it contains, in order of most-recently-added first.
	** If no Implicator evaluate()s to a non-null Object, then the Chain's
	** own Object-of-last-resort is returned.  That Object may be null.
	**<p>
	** An instance of Implicator.Chain is itself an Implicator.
	** This lets you arrange chains of Implicator.Chains and evaluate them.
	** It is also a pathway to infinite recursion if there are any circular references.
	**<p>
	** An Implicator.Chain expresses the Chain of Responsibility design pattern.
	** It is not, however, implemented using a linked-list, as "Design Patterns" shows.
	** As a result, a single Implicator instance can be a member of more than
	** one Implicator.Chain without any ill effects.
	** It will be evaluated in each chain at the appropriate time, with the
	** appropriate arg passed to evaluate().
	** Another consequence of not being a linked-list is that no Implicator knows
	** which Implicator.Chain it's in, nor any of the other Implicators before or
	** after it in the chain.
	*/
	public static class Chain
	  extends Implicator
	{
		/** Object-of-last-resort.  Accessible and assignable in subclasses. */
		protected Object myLast;

		/** An Arrayer holds the current series of Implicators. */
		protected Arrayer myImps;

		/**
		** Create an empty instance, having the given Object-of-last-resort.
		** That Object will only be returned if no Implicator returns an Object.
		** The Object ofLastResort may be null, and null will be returned if
		** no Implicators return an Object before the chain is exhausted.
		*/
		public
		Chain( Object ofLastResort )
		{
			myLast = ofLastResort;
			myImps = new Arrayer( new Implicator[ 3 ] );
		}


		/** Ensure there is at least the given capacity. */
		public void
		capacity( int capacity )
		{  myImps.enough( capacity );  }


		/** Remove all Implicators.  Does not affect the Object-of-last-resort. */
		public void
		clear()
		{  myImps.clear();  }

		/**
		** Add the given Implicator.
		** If the Implicator is null, it is quietly ignored.
		*/
		public void
		add( Implicator imp )
		{  myImps.append( imp );  }

		/**
		** Evaluate the previously add()'ed Implicators, returning the first non-null result.
		** If no Implicator returns a valid result, the Object-of-last-resort is returned.
		** If there are no Implicators in the chain, the Object-of-last-resort is always returned.
		**<p>
		** The chain of Implicators are evaluated most-recently-added first,
		** then progressing back through previously added ones.
		**<p>
		** The given Object may be anything, or it may be null,
		** depending on what the chain of Implicators needs or expects.
		** It is simply passed along to each Implicator's evaluate() method in turn,
		** but is otherwise unused by this method.
		*/
		public Object
		evaluate( Object given )
		{
			// Scan down the Arrayer's actual internal array.
			Implicator[] imps = (Implicator[]) myImps.array();

			for ( int i = myImps.size();  --i >= 0;  )
			{
				Object result = imps[ i ].evaluate( given );
				if ( result != null )
					return ( result );
			}

			// Getting here, no Implicators were successful, so return Object-of-last-resort.
			return ( myLast );
		}
	}


}

