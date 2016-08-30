using System;

namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// Specifies identifiers to indicate the return value of a task dialog box.
	/// </summary>
	public enum TaskDialogSimpleResult
	{
		// IMPORTANT
		// Values 0 - 8 are in a very specific order to match that of the DialogResult
		//enum in the WinForms namespace. This explains the skipped numbers, as they
		//are unused values (such as Abort and Ignore). Close is not technically in the
		//original enum, but is consistent with Win32 TaskDialogIndirect behavior.

		/// <summary>
		/// Nothing is returned from the dialog box.
		/// </summary>
		None = 0,
		/// <summary>
		/// The dialog box return value is Ok (usually sent from a button
		/// labeled Ok).
		/// </summary>
		Ok = 1,
		/// <summary>
		/// The dialog box return value is Cancel (usually sent from a button
		/// labeled Cancel). Can also be as a result of clicking the red X in
		/// the top right corner.
		/// </summary>
		Cancel = 2,
		/// <summary>
		/// The dialog box return value is Retry (usually sent from a button
		/// labeled Retry).
		/// </summary>
		Retry = 4,
		/// <summary>
		/// The dialog box return value is Yes (usually sent from a button
		/// labeled Yes).
		/// </summary>
		Yes = 6,
		/// <summary>
		/// The dialog box return value is No (usually sent from a button
		/// labeled No).
		/// </summary>
		No = 7,
		/// <summary>
		/// The dialog box return value is Close (usually sent from a button
		/// labeled Close),
		/// </summary>
		Close = 8,
		/// <summary>
		/// The dialog box return value is a custom command (usually sent from
		/// a command button).
		/// </summary>
		Command = 20,
		/// <summary>
		/// The dialog box return value is a custom button (usually sent from
		/// a custom-defined button).
		/// </summary>
		Custom = 21
	}
	/// <summary>
	/// Specifies data for the return values of a task dialog box.
	/// </summary>
	public struct TaskDialogResult
	{
		/// <summary>
		/// Represents a result with no data.
		/// </summary>
		public static readonly TaskDialogResult Empty = new TaskDialogResult();

		/// <summary>
		/// Gets the <see cref="T:TaskDialogSimpleResult"/> of the TaskDialog.
		/// </summary>
		public TaskDialogSimpleResult Result { get; private set; }
		/// <summary>
		/// Gets a value indicating whether or not the verification checkbox
		/// was checked. A null value indicates that the checkbox wasn't shown.
		/// </summary>
		public bool? VerificationChecked { get; private set; }
		/// <summary>
		/// Gets the zero-based index of the radio button that was clicked.
		/// A null value indicates that no radio button was clicked.
		/// </summary>
		public int? RadioButtonResult { get; private set; }
		/// <summary>
		/// Gets the zero-based index of the command button that was clicked.
		/// A null value indicates that no command button was clicked.
		/// </summary>
		public int? CommandButtonResult { get; private set; }
		/// <summary>
		/// Gets the zero-based index of the custom button that was clicked.
		/// A null value indicates that no custom button was clicked.
		/// </summary>
		public int? CustomButtonResult { get; private set; }

		/// <summary>
		/// Initializes a new instance of the <see cref="TaskDialogResult"/> class.
		/// </summary>
		/// <param name="result">The simple TaskDialog result.</param>
		/// <param name="verificationChecked">Wether the verification checkbox was checked.</param>
		/// <param name="radioButtonResult">The radio button result, if any.</param>
		/// <param name="commandButtonResult">The command button result, if any.</param>
		/// <param name="customButtonResult">The custom button result, if any.</param>
		internal TaskDialogResult(TaskDialogSimpleResult result, bool? verificationChecked = null, int? radioButtonResult = null, int? commandButtonResult = null, int? customButtonResult = null)
			: this()
		{
			Result = result;
			VerificationChecked = verificationChecked;
			RadioButtonResult = radioButtonResult;
			CommandButtonResult = commandButtonResult;
			CustomButtonResult = customButtonResult;
		}

		/// <summary>
		/// Implements the operator ==.
		/// </summary>
		/// <param name="a">The first operand.</param>
		/// <param name="b">The second operand.</param>
		/// <returns>The result of the operator.</returns>
		public static bool operator ==(TaskDialogResult a, TaskDialogResult b)
		{
			return a.Equals(b);
		}
		/// <summary>
		/// Implements the operator !=.
		/// </summary>
		/// <param name="a">The first operand.</param>
		/// <param name="b">The second operand.</param>
		/// <returns>The result of the operator.</returns>
		public static bool operator !=(TaskDialogResult a, TaskDialogResult b)
		{
			return !(a == b);
		}

		/// <summary>
		/// Determines whether the specified <see cref="System.Object"/> is equal to this instance.
		/// </summary>
		/// <param name="obj">The <see cref="System.Object"/> to compare with this instance.</param>
		/// <returns>
		/// 	<c>true</c> if the specified <see cref="System.Object"/> is equal to this instance; otherwise, <c>false</c>.
		/// </returns>
		/// <exception cref="T:System.NullReferenceException">
		/// The <paramref name="obj"/> parameter is null.
		/// </exception>
		public override bool Equals(object obj)
		{
			if (obj == null) return false;

			if (ReferenceEquals(this, obj)) return true;

			bool result = false;

			try
			{
				var tdr = (TaskDialogResult)obj;

				result = this.Result == tdr.Result
					&& this.VerificationChecked == tdr.VerificationChecked
					&& this.RadioButtonResult == tdr.RadioButtonResult
					&& this.CommandButtonResult == tdr.CommandButtonResult
					&& this.CustomButtonResult == tdr.CustomButtonResult;
			}
			catch (InvalidCastException) // obj isn't of type TaskDialogResult
			{
				result = false;
			}
			catch (NullReferenceException) // obj is null, 
			{
				result = false;
			}

			return result;
		}
		/// <summary>
		/// Returns a hash code for this instance.
		/// </summary>
		/// <returns>
		/// A hash code for this instance, suitable for use in hashing algorithms and data structures like a hash table. 
		/// </returns>
		public override int GetHashCode()
		{
			return Result.GetHashCode() ^ VerificationChecked.GetHashCode()
				^ RadioButtonResult.GetHashCode() ^ CommandButtonResult.GetHashCode()
				^ CustomButtonResult.GetHashCode();
		}
		/// <summary>
		/// Returns a <see cref="System.String"/> that represents this instance.
		/// </summary>
		/// <returns>
		/// A <see cref="System.String"/> that represents this instance.
		/// </returns>
		public override string ToString()
		{
			string text = "Result: " + Result.ToString();

			if (VerificationChecked.HasValue)
				text += ", VerificationChecked: " + VerificationChecked.Value.ToString();
			if (RadioButtonResult.HasValue)
				text += ", RadioButtonResult: " + RadioButtonResult.Value.ToString();
			if (CommandButtonResult.HasValue)
				text += ", CommandButtonResult: " + CommandButtonResult.Value.ToString();
			if (CustomButtonResult.HasValue)
				text += ", CustomButtonResult: " + CustomButtonResult.Value.ToString();

			return text;
		}
	}
}
