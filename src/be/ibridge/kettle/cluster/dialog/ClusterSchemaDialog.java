package be.ibridge.kettle.cluster.dialog;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import be.ibridge.kettle.cluster.ClusterSchema;
import be.ibridge.kettle.cluster.SlaveServer;
import be.ibridge.kettle.core.ColumnInfo;
import be.ibridge.kettle.core.Const;
import be.ibridge.kettle.core.Props;
import be.ibridge.kettle.core.WindowProperty;
import be.ibridge.kettle.core.widget.TableView;
import be.ibridge.kettle.trans.step.BaseStepDialog;


/**
 * 
 * Dialog that allows you to edit the settings of the cluster schema
 * 
 * @see ClusterSchema
 * @author Matt
 * @since 17-11-2006
 *
 */

public class ClusterSchemaDialog extends Dialog 
{
	private ClusterSchema clusterSchema;
	
	private Shell     shell;

    // Name
	private Text     wName;

    // Servers
    private TableView     wServers;

	private Button    wOK, wCancel;
	
    private ModifyListener lsMod;

	private Props     props;

    private int middle;
    private int margin;

    private ClusterSchema originalSchema;
    private boolean ok;

    private Button wAdd;

    private Button wDel;

    private Button wEdit;
    
	public ClusterSchemaDialog(Shell par, ClusterSchema clusterSchema)
	{
		super(par, SWT.NONE);
		this.clusterSchema=(ClusterSchema) clusterSchema.clone();
        this.originalSchema=clusterSchema;
		props=Props.getInstance();
        ok=false;
	}
	
	public boolean open() 
	{
		Shell parent = getParent();
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN );
 		props.setLook(shell);
		
		lsMod = new ModifyListener() 
		{
			public void modifyText(ModifyEvent e) 
			{
				clusterSchema.setChanged();
			}
		};

		middle = props.getMiddlePct();
		margin = Const.MARGIN;

		FormLayout formLayout = new FormLayout ();
		formLayout.marginWidth  = Const.FORM_MARGIN;
		formLayout.marginHeight = Const.FORM_MARGIN;
		
		shell.setText("Clustering schema dialog");
		shell.setLayout (formLayout);
 		
		// First, add the buttons...
		
		// Buttons
		wOK     = new Button(shell, SWT.PUSH); 
		wOK.setText(" &OK ");

		wCancel = new Button(shell, SWT.PUSH); 
		wCancel.setText(" &Cancel ");

		Button[] buttons = new Button[] { wOK, wCancel };
		BaseStepDialog.positionBottomButtons(shell, buttons, margin, null);
		
		// The rest stays above the buttons, so we added those first...
        
        // What's the schema name??
        Label wlName = new Label(shell, SWT.RIGHT); 
        props.setLook(wlName);
        wlName.setText("Schema name  ");
        FormData fdlServiceURL = new FormData();
        fdlServiceURL.top   = new FormAttachment(0, 0);
        fdlServiceURL.left  = new FormAttachment(0, 0);  // First one in the left top corner
        fdlServiceURL.right = new FormAttachment(middle, 0);
        wlName.setLayoutData(fdlServiceURL);

        wName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
        props.setLook(wName);
        wName.addModifyListener(lsMod);
        FormData fdServiceURL = new FormData();
        fdServiceURL.top  = new FormAttachment(0, 0);
        fdServiceURL.left = new FormAttachment(middle, margin); // To the right of the label
        fdServiceURL.right= new FormAttachment(95, 0);
        wName.setLayoutData(fdServiceURL);

        // Schema servers:
        Label wlServers = new Label(shell, SWT.RIGHT);
        wlServers.setText("Slave servers  ");
        props.setLook(wlServers);
        FormData fdlServers=new FormData();
        fdlServers.left = new FormAttachment(0, 0);
        fdlServers.right = new FormAttachment(middle, 0);
        fdlServers.top  = new FormAttachment(wName, margin);
        wlServers.setLayoutData(fdlServers);
        
        // Some buttons to manage...
        wAdd = new Button(shell, SWT.PUSH);
        wAdd.setText("Add slave server");
        props.setLook(wAdd);
        FormData fdAdd=new FormData();
        fdAdd.right= new FormAttachment(100, 0);
        fdAdd.top  = new FormAttachment(wlServers, 5*margin);
        wAdd.setLayoutData(fdAdd);
        wAdd.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { addSlaveServer(); }});

        // Some buttons to manage...
        wEdit = new Button(shell, SWT.PUSH);
        wEdit.setText("Edit slave server");
        props.setLook(wEdit);
        FormData fdEdit=new FormData();
        fdEdit.right= new FormAttachment(100, 0);
        fdEdit.top  = new FormAttachment(wAdd, 2*margin);
        wEdit.setLayoutData(fdEdit);
        wEdit.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { editSlaveServer(); }});

        // Some buttons to manage...
        wDel = new Button(shell, SWT.PUSH);
        wDel.setText("Delete slave server");
        props.setLook(wDel);
        FormData fdDel=new FormData();
        fdDel.right= new FormAttachment(100, 0);
        fdDel.top  = new FormAttachment(wEdit, 2*margin);
        wDel.setLayoutData(fdDel);
        wDel.addSelectionListener(new SelectionAdapter() { public void widgetSelected(SelectionEvent e) { removeSlaveServer(); }});

        ColumnInfo[] partitionColumns = new ColumnInfo[] { 
                new ColumnInfo( "Service URL", ColumnInfo.COLUMN_TYPE_TEXT, true, true), //$NON-NLS-1$
        };
        wServers = new TableView(shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE, partitionColumns, 1, lsMod, props);
        wServers.setReadonly(true);
        props.setLook(wServers);
        FormData fdServers = new FormData();
        fdServers.left = new FormAttachment(middle, margin );
        fdServers.right = new FormAttachment(wDel, -2*margin);
        fdServers.top = new FormAttachment(wName, margin);
        fdServers.bottom = new FormAttachment(wOK, -margin * 2);
        wServers.setLayoutData(fdServers);
        wServers.table.addSelectionListener(new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { editSlaveServer(); }});
		
		// Add listeners
		wOK.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { ok(); } } );
        wCancel.addListener(SWT.Selection, new Listener () { public void handleEvent (Event e) { cancel(); } } );
		
        SelectionAdapter selAdapter=new SelectionAdapter() { public void widgetDefaultSelected(SelectionEvent e) { ok(); } };
		wName.addSelectionListener(selAdapter);

		// Detect X or ALT-F4 or something that kills this window...
		shell.addShellListener(	new ShellAdapter() { public void shellClosed(ShellEvent e) { cancel(); } } );
	
		getData();

		BaseStepDialog.setSize(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) display.sleep();
		}
		return ok;
	}
	
    private void editSlaveServer()
    {
        int idx = wServers.getSelectionIndex();
        if (idx>=0)
        {
            SlaveServer slaveServer = (SlaveServer) clusterSchema.getSlaveServers().get(idx);
            SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
            if (dialog.open())
            {
                refreshSlaveServers();
            }
        }
    }

    private void removeSlaveServer()
    {
        int idx = wServers.getSelectionIndex();
        if (idx>=0)
        {
            clusterSchema.getSlaveServers().remove(idx);
            refreshSlaveServers();
        }
    }

    private void addSlaveServer()
    {
        SlaveServer slaveServer = new SlaveServer();
        SlaveServerDialog dialog = new SlaveServerDialog(shell, slaveServer);
        if (dialog.open())
        {
            clusterSchema.getSlaveServers().add(slaveServer);
            refreshSlaveServers();
        }
    }

    public void dispose()
	{
		props.setScreen(new WindowProperty(shell));
		shell.dispose();
	}
    
    public void getData()
	{
		wName.setText( Const.NVL(clusterSchema.getName(), "") );

        refreshSlaveServers();
        
		wName.setFocus();
	}
    
	private void refreshSlaveServers()
    {
        wServers.clearAll(false);
        List slaveServers = clusterSchema.getSlaveServers();
        for (int i=0;i<slaveServers.size();i++)
        {
            TableItem item = new TableItem(wServers.table, SWT.NONE);
            SlaveServer slaveServer = (SlaveServer)slaveServers.get(i);
            if (slaveServer.getHostname()!=null) item.setText(1, slaveServer.getHostname());
        }
        wServers.removeEmptyRows();
        wServers.setRowNums();
        wServers.optWidth(true);
    }

    private void cancel()
	{
		originalSchema = null;
		dispose();
	}
	
	public void ok()
	{
        getInfo();
        originalSchema.setName(clusterSchema.getName());
        originalSchema.setSlaveServers(clusterSchema.getSlaveServers());
        originalSchema.setChanged();

        ok=true;
        
        dispose();
	}
    
    // Get dialog info in securityService
	private void getInfo()
    {
        clusterSchema.setName(wName.getText());
        // The cluster-schema is maneged by the buttons.
    }
}