namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// Defines localized strings for the emulated dialog.
	/// </summary>
	/// <remarks>
	/// Create a derived type from this type with the language of your choice or
	/// one using the existing localization technique of your choice.
	/// An English one is provided by default.
	/// </remarks>
	public abstract class TaskDialogStrings
	{
		/// <summary>
		/// Gets the text to show on the OK common button.
		/// </summary>
		public abstract string CommonButton_OK { get; }
		/// <summary>
		/// Gets the text to show on the Yes common button.
		/// </summary>
		public abstract string CommonButton_Yes { get; }
		/// <summary>
		/// Gets the text to show on the No common button.
		/// </summary>
		public abstract string CommonButton_No { get; }
		/// <summary>
		/// Gets the text to show on the Cancel common button.
		/// </summary>
		public abstract string CommonButton_Cancel { get; }
		/// <summary>
		/// Gets the text to show on the Retry common button.
		/// </summary>
		public abstract string CommonButton_Retry { get; }
		/// <summary>
		/// Gets the text to show on the Close common button.
		/// </summary>
		public abstract string CommonButton_Close { get; }

		/// <summary>
		/// Gets the text to show on the expander toggle button to show details.
		/// </summary>
		public abstract string ExpandedInfo_Show { get; }
		/// <summary>
		/// Gets the text to show on the expander toggle button to hide details.
		/// </summary>
		public abstract string ExpandedInfo_Hide { get; }
	}
	/// <summary>
	/// Defines English localized strings for the emulated dialog.
	/// </summary>
	/// <remarks>
	/// Separate types, all derived from TaskDialogStrings, could be made for
	/// Spanish, German, etc., or you could opt to have one class that retrieves
	/// values from whatever existing localization method you have.
	/// </remarks>
	public class EnglishTaskDialogStrings : TaskDialogStrings
	{
		/// <summary>
		/// Gets the text to show on the OK common button.
		/// </summary>
		public override string CommonButton_OK
		{
			// Here I am hard-coding some English strings
			// But you could retrieve values from a ResourceDictionary or Resx
			// Don't forget to add an underscore to denote the accelerator key!
			get { return "_OK"; }
		}
		/// <summary>
		/// Gets the text to show on the Yes common button.
		/// </summary>
		public override string CommonButton_Yes
		{
			get { return "_Yes"; }
		}
		/// <summary>
		/// Gets the text to show on the No common button.
		/// </summary>
		public override string CommonButton_No
		{
			get { return "_No"; }
		}
		/// <summary>
		/// Gets the text to show on the Cancel common button.
		/// </summary>
		public override string CommonButton_Cancel
		{
			get { return "_Cancel"; }
		}
		/// <summary>
		/// Gets the text to show on the Retry common button.
		/// </summary>
		public override string CommonButton_Retry
		{
			get { return "_Retry"; }
		}
		/// <summary>
		/// Gets the text to show on the Close common button.
		/// </summary>
		public override string CommonButton_Close
		{
			get { return "_Close"; }
		}

		/// <summary>
		/// Gets the text to show on the expander toggle button to show details.
		/// </summary>
		public override string ExpandedInfo_Show
		{
			get { return "Show details"; }
		}
		/// <summary>
		/// Gets the text to show on the expander toggle button to hide details.
		/// </summary>
		public override string ExpandedInfo_Hide
		{
			get { return "Hide details"; }
		}

		/// <summary>
		/// Initializes a new instance of the <see cref="T:EnglishTaskDialogStrings"/> class.
		/// </summary>
		internal EnglishTaskDialogStrings()
		{
		}
	}
}
