// 
// Copyright (c) 2010 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 
using System;
using System.Collections;
using System.ComponentModel;
using System.ComponentModel.Design;
using System.Drawing;
using System.Drawing.Design;
using System.Drawing.Drawing2D;
using System.Windows.Forms;
using System.Windows.Forms.Design;

namespace Ch.Cyberduck.Ui.Winforms.Controls
{
    [DefaultProperty("SelectedPanel")]
    [DefaultEvent("SelectedIndexChanged")]
    [Designer(typeof (PanelManagerDesigner))]
    public class PanelManager : Control
    {
        /// <summary> 
        /// Required designer variable.
        /// </summary>
        private Container components;

        private ManagedPanel m_SelectedPanel;
        private ManagedPanel oldSelection;

        public PanelManager()
        {
            // This call is required by the Windows.Forms Form Designer.
            InitializeComponent();
        }

        //ManagedPanels
        [Editor(typeof (ManagedPanelCollectionEditor), typeof (UITypeEditor))]
        public ControlCollection ManagedPanels
        {
            get { return base.Controls; }
        }


        //SelectedPanel
        [TypeConverter(typeof (SelectedPanelConverter))]
        public ManagedPanel SelectedPanel
        {
            get { return m_SelectedPanel; }
            set
            {
                if (m_SelectedPanel == value) return;
                m_SelectedPanel = value;
                OnSelectedPanelChanged(EventArgs.Empty);
            }
        }


        //SelectedIndex
        [Browsable(false)]
        public int SelectedIndex
        {
            get { return ManagedPanels.IndexOf(SelectedPanel); }
            set
            {
                if (value == -1)
                    SelectedPanel = null;
                else
                    SelectedPanel = (ManagedPanel) ManagedPanels[value];
            }
        }


        //DefaultSize
        protected override Size DefaultSize
        {
            get { return new Size(200, 100); }
        }

        /// <summary> 
        /// Clean up any resources being used.
        /// </summary>
        protected override void Dispose(bool disposing)
        {
            if (disposing)
            {
                if (components != null)
                {
                    components.Dispose();
                }
            }
            base.Dispose(disposing);
        }

        /// <summary> 
        /// Required method for Designer support - do not modify 
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            components = new Container();
        }

        public event EventHandler SelectedIndexChanged;


        protected void OnSelectedPanelChanged(EventArgs e)
        {
            if (oldSelection != null)
                oldSelection.Visible = false;

            if (m_SelectedPanel != null)
                (m_SelectedPanel).Visible = true;

            bool tabChanged = false;
            if (m_SelectedPanel == null)
                tabChanged = (oldSelection != null);
            else
                tabChanged = (!m_SelectedPanel.Equals(oldSelection));

            if (tabChanged && Created)
                if (SelectedIndexChanged != null)
                    SelectedIndexChanged(this, EventArgs.Empty);

            oldSelection = m_SelectedPanel;
            m_SelectedPanel.Focus();
        }


        protected override void OnControlAdded(ControlEventArgs e)
        {
            if ((e.Control is ManagedPanel) == false)
                throw new ArgumentException(
                    "Only Mangel.Controls.ManagedPanels can be added to a Mangel.Controls.PanelManger.");

            if (SelectedPanel != null)
                (SelectedPanel).Visible = false;

            SelectedPanel = (ManagedPanel) e.Control;
            e.Control.Visible = true;
            base.OnControlAdded(e);
        }


        protected override void OnControlRemoved(ControlEventArgs e)
        {
            if (e.Control is ManagedPanel)
            {
                if (ManagedPanels.Count > 0)
                    SelectedIndex = 0;
                else
                    SelectedPanel = null;
            }
            base.OnControlRemoved(e);
        }
    }


    [Designer(typeof (ManagedPanelDesigner))]
    [ToolboxItem(false)]
    public class ManagedPanel : ScrollableControl
    {
        public ManagedPanel()
        {
            base.Dock = DockStyle.Fill;
            SetStyle(ControlStyles.ResizeRedraw, true);
        }


        [Browsable(false), EditorBrowsable(EditorBrowsableState.Never)]
        [DefaultValue(typeof (DockStyle), "Fill")]
        public override DockStyle Dock
        {
            get { return base.Dock; }
            set { base.Dock = DockStyle.Fill; }
        }


        [Browsable(false), EditorBrowsable(EditorBrowsableState.Never)]
        [DefaultValue(typeof (AnchorStyles), "None")]
        public override AnchorStyles Anchor
        {
            get { return AnchorStyles.None; }
            set { base.Anchor = AnchorStyles.None; }
        }


        protected override void OnLocationChanged(EventArgs e)
        {
            base.OnLocationChanged(e);
            base.Location = Point.Empty;
        }


        protected override void OnSizeChanged(EventArgs e)
        {
            base.OnSizeChanged(e);
            if (Parent == null)
                Size = Size.Empty;
            else
                Size = Parent.ClientSize;
        }


        protected override void OnParentChanged(EventArgs e)
        {
            if ((Parent is PanelManager) == false && Parent != null)
                throw new ArgumentException("Managed Panels may only be added to a Panel Manager.");
            base.OnParentChanged(e);
        }
    }

    public class PanelManagerDesigner : ParentControlDesigner
    {
        private readonly DesignerVerbCollection m_verbs = new DesignerVerbCollection();
        private IDesignerHost m_DesignerHost;
        private ISelectionService m_SelectionService;


        public PanelManagerDesigner()
        {
            DesignerVerb verb1 = new DesignerVerb("Add MangedPanel", OnAddPanel);
            DesignerVerb verb2 = new DesignerVerb("Remove ManagedPanel", OnRemovePanel);
            m_verbs.AddRange(new[] {verb1, verb2});
        }

        private PanelManager HostControl
        {
            get { return (PanelManager) Control; }
        }


        public override DesignerVerbCollection Verbs
        {
            get
            {
                if (m_verbs.Count == 2)
                    m_verbs[1].Enabled = HostControl.ManagedPanels.Count > 0;
                return m_verbs;
            }
        }


        public IDesignerHost DesignerHost
        {
            get
            {
                if (m_DesignerHost == null)
                    m_DesignerHost = (IDesignerHost) GetService(typeof (IDesignerHost));
                return m_DesignerHost;
            }
        }


        public ISelectionService SelectionService
        {
            get
            {
                if (m_SelectionService == null)
                    m_SelectionService = (ISelectionService) GetService(typeof (ISelectionService));

                return m_SelectionService;
            }
        }

        protected override void OnPaintAdornments(PaintEventArgs pe)
        {
            // Don't want DrawGrid Dots.
        }


        private void OnAddPanel(Object sender, EventArgs e)
        {
            Control.ControlCollection oldManagedPanels = HostControl.Controls;

            RaiseComponentChanging(TypeDescriptor.GetProperties(HostControl)["ManagedPanels"]);

            ManagedPanel P = (ManagedPanel) DesignerHost.CreateComponent(typeof (ManagedPanel));
            P.Text = P.Name;
            HostControl.ManagedPanels.Add(P);

            RaiseComponentChanged(TypeDescriptor.GetProperties(HostControl)["ManagedPanels"], oldManagedPanels,
                                  HostControl.ManagedPanels);
            HostControl.SelectedPanel = P;

            SetVerbs();
        }


        private void OnRemovePanel(Object sender, EventArgs e)
        {
            Control.ControlCollection oldManagedPanels = HostControl.Controls;

            if (HostControl.SelectedIndex < 0) return;

            RaiseComponentChanging(TypeDescriptor.GetProperties(HostControl)["TabPages"]);

            DesignerHost.DestroyComponent(HostControl.ManagedPanels[HostControl.SelectedIndex]);

            RaiseComponentChanged(TypeDescriptor.GetProperties(HostControl)["ManagedPanels"], oldManagedPanels,
                                  HostControl.ManagedPanels);

            SelectionService.SetSelectedComponents(new IComponent[] {HostControl}, SelectionTypes.Auto);

            SetVerbs();
        }


        private void SetVerbs()
        {
            Verbs[1].Enabled = HostControl.ManagedPanels.Count == 1;
        }


        protected override void PostFilterProperties(IDictionary properties)
        {
            properties.Remove("AutoScroll");
            properties.Remove("AutoScrollMargin");
            properties.Remove("AutoScrollMinSize");
            properties.Remove("Text");
            base.PostFilterProperties(properties);
        }


        [Obsolete("This method has been deprecated. Use InitializeNewComponent instead.  http://go.microsoft.com/fwlink/?linkid=14202")]
        public override void OnSetComponentDefaults()
        {
            HostControl.ManagedPanels.Add((ManagedPanel) DesignerHost.CreateComponent(typeof (ManagedPanel)));
            HostControl.ManagedPanels.Add((ManagedPanel) DesignerHost.CreateComponent(typeof (ManagedPanel)));
            PanelManager pm = (PanelManager) Control;
            pm.ManagedPanels[0].Text = pm.ManagedPanels[0].Name;
            pm.ManagedPanels[1].Text = pm.ManagedPanels[1].Name;
            HostControl.SelectedIndex = 0;
        }
    }


    public class ManagedPanelDesigner : ScrollableControlDesigner
    {
        private readonly DesignerVerbCollection m_verbs = new DesignerVerbCollection();
        private ISelectionService m_SelectionService;

        public ManagedPanelDesigner()
        {
            DesignerVerb verb1 = new DesignerVerb("Select PanelManager", OnSelectManager);
            m_verbs.Add(verb1);
        }

        private ManagedPanel HostControl
        {
            get { return (ManagedPanel) Control; }
        }


        public ISelectionService SelectionService
        {
            get
            {
                if (m_SelectionService == null)
                    m_SelectionService = (ISelectionService) GetService(typeof (ISelectionService));

                return m_SelectionService;
            }
        }


        public override SelectionRules SelectionRules
        {
            get { return SelectionRules.Visible; }
        }


        public override DesignerVerbCollection Verbs
        {
            get { return m_verbs; }
        }

        private void OnSelectManager(Object sender, EventArgs e)
        {
            if (HostControl.Parent != null)
                SelectionService.SetSelectedComponents(new Component[] {HostControl.Parent});
        }

        protected override void OnPaintAdornments(PaintEventArgs pe)
        {
            base.OnPaintAdornments(pe);
            Color penColor;
            if (Control.BackColor.GetBrightness() >= 0.5)
                penColor = ControlPaint.Dark(Control.BackColor);
            else
                penColor = Color.White;

            Pen dashedPen = new Pen(penColor);
            Rectangle borderRectangle = Control.ClientRectangle;
            borderRectangle.Width -= 1;
            borderRectangle.Height -= 1;
            dashedPen.DashStyle = DashStyle.Dash;
            pe.Graphics.DrawRectangle(dashedPen, borderRectangle);
            dashedPen.Dispose();
        }


        protected override void PostFilterProperties(IDictionary properties)
        {
            properties.Remove("Anchor");
            properties.Remove("TabStop");
            properties.Remove("TabIndex");
            base.PostFilterProperties(properties);
        }


        [Obsolete("This method has been deprecated. Use InitializeNewComponent instead.  http://go.microsoft.com/fwlink/?linkid=14202")]
        public override void OnSetComponentDefaults()
        {
            base.OnSetComponentDefaults();
            Control.Visible = true;
        }
    }

    public class ManagedPanelCollectionEditor : CollectionEditor
    {
        public ManagedPanelCollectionEditor(Type type) : base(type)
        {
        }

        protected override Type CreateCollectionItemType()
        {
            return typeof (ManagedPanel);
        }
    }

    public class SelectedPanelConverter : ReferenceConverter
    {
        public SelectedPanelConverter() : base(typeof (ManagedPanel))
        {
        }

        protected override bool IsValueAllowed(ITypeDescriptorContext context, object value)
        {
            if (context != null)
            {
                PanelManager pm = (PanelManager) context.Instance;
                return pm.ManagedPanels.Contains((ManagedPanel) value);
            }
            return false;
        }
    }
}
