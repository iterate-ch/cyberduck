using System;

namespace Ch.Cyberduck.Core.TaskDialog
{
	/// <summary>
	/// Provides data for the <see cref="T:TaskDialogInterop.TaskDialog.Showing"/> event.
	/// </summary>
	public class TaskDialogShowingEventArgs : EventArgs
	{
		/// <summary>
		/// Gets the configuration options for the TaskDialog.
		/// </summary>
		public TaskDialogOptions ConfigurationOptions { get; private set; }

		/// <summary>
		/// Initializes a new instance of the <see cref="TaskDialogShowingEventArgs"/> class.
		/// </summary>
		/// <param name="configOptions">The configuration options for the TaskDialog.</param>
		internal TaskDialogShowingEventArgs(ref TaskDialogOptions configOptions)
		{
			ConfigurationOptions = configOptions;
		}
	}
	/// <summary>
	/// Provides data for the <see cref="T:TaskDialogInterop.TaskDialog.Closed"/> event.
	/// </summary>
	public class TaskDialogClosedEventArgs : EventArgs
	{
		/// <summary>
		/// Gets the result of the TaskDialog.
		/// </summary>
		public TaskDialogResult Result { get; private set; }

		/// <summary>
		/// Initializes a new instance of the <see cref="TaskDialogClosedEventArgs"/> class.
		/// </summary>
		/// <param name="result">The result of the TaskDialog.</param>
		internal TaskDialogClosedEventArgs(TaskDialogResult result)
		{
			Result = result;
		}
	}
	/// <summary>
	/// Represents the method that will handle the
	/// <see cref="T:TaskDialogInterop.TaskDialog.Showing"/> event.
	/// </summary>
	/// <param name="sender">The source of the event.</param>
	/// <param name="e">
	/// A <see cref="T:TaskDialogInterop.TaskDialogShowingEventArgs"/> that contains the event data.
	/// </param>
	public delegate void TaskDialogShowingEventHandler(object sender, TaskDialogShowingEventArgs e);
	/// <summary>
	/// Represents the method that will handle the
	/// <see cref="T:TaskDialogInterop.TaskDialog.Closed"/> event.
	/// </summary>
	/// <param name="sender">The source of the event.</param>
	/// <param name="e">
	/// A <see cref="T:TaskDialogInterop.TaskDialogClosedEventArgs"/> that contains the event data.
	/// </param>
	public delegate void TaskDialogClosedEventHandler(object sender, TaskDialogClosedEventArgs e);
}
