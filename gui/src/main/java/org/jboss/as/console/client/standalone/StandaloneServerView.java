package org.jboss.as.console.client.standalone;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.as.console.client.core.DisposableViewImpl;
import org.jboss.as.console.client.shared.viewframework.builder.SimpleLayout;
import org.jboss.ballroom.client.widgets.ContentGroupLabel;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.ballroom.client.widgets.icons.Icons;
import org.jboss.ballroom.client.widgets.tools.ToolButton;
import org.jboss.ballroom.client.widgets.tools.ToolStrip;
import org.jboss.ballroom.client.widgets.window.Feedback;


/**
 * @author Heiko Braun
 * @date 6/7/11
 */
public class StandaloneServerView extends DisposableViewImpl implements StandaloneServerPresenter.MyView {

    private StandaloneServerPresenter presenter;
    private Label headline;
    private DeckPanel reloadPanel;
    private Form<StandaloneServer> form;

    @Override
    public Widget createWidget() {

        final ToolStrip toolStrip = new ToolStrip();

        toolStrip.addToolButtonRight(new ToolButton("Reload", new ClickHandler(){
            @Override
            public void onClick(ClickEvent event) {
                Feedback.confirm("Reload server configuration",
                        "Do you want ot reload the server configuration for server " + form.getEditedEntity().getName() + "?",
                        new Feedback.ConfirmationHandler() {
                            @Override
                            public void onConfirmation(boolean isConfirmed) {
                                if (isConfirmed) {
                                    presenter.onReloadServerConfig();
                                }
                            }
                        });
            }
        }));

        headline = new Label("HEADLINE");

        headline.setStyleName("content-header-label");

        form = new Form<StandaloneServer>(StandaloneServer.class);
        form.setNumColumns(2);

        TextItem codename = new TextItem("releaseCodename", "Code Name");
        TextItem version = new TextItem("releaseVersion", "Release version");
        TextItem state = new TextItem("serverState", "Server State");

        form.setFields(codename, version, state);



        // ----

        reloadPanel = new DeckPanel();
        reloadPanel.setStyleName("fill-layout-width");

        // ----

        VerticalPanel configUptodate = new VerticalPanel();
        ContentGroupLabel label = new ContentGroupLabel("Server Configuration");
        label.getElement().setAttribute("style", "padding-top:15px;");
        configUptodate.add(label);

        HorizontalPanel uptodateContent = new HorizontalPanel();
        uptodateContent.setStyleName("status-panel");
        uptodateContent.addStyleName("serverUptoDate");

        Image img = new Image(Icons.INSTANCE.statusGreen_small());
        HTML desc = new HTML("The server configuration seems uptodate!");
        uptodateContent.add(desc);
        uptodateContent.add(img);

        img.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:top");
        desc.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:top");

        configUptodate.add(uptodateContent);

        // --

        VerticalPanel configNeedsUpdate = new VerticalPanel();
        ContentGroupLabel label2 = new ContentGroupLabel("Server Configuration");
        label2.getElement().setAttribute("style", "padding-top:15px;");
        configNeedsUpdate.add(label2);

        HorizontalPanel staleContent = new HorizontalPanel();
        staleContent.setStyleName("status-panel");
        staleContent.addStyleName("serverNeedsUpdate");

        Image img2 = new Image(Icons.INSTANCE.statusRed_small());
        HTML desc2 = new HTML("The server configuration needs to be reloaded or the server restartet!");
        staleContent.add(desc2);
        staleContent.add(img2);

        img2.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:top");
        desc2.getElement().getParentElement().setAttribute("style", "padding:15px;vertical-align:top");

        configNeedsUpdate.add(staleContent);

        // ----

        reloadPanel.add(configUptodate);
        reloadPanel.add(configNeedsUpdate);
        reloadPanel.showWidget(0);

        SimpleLayout layout = new SimpleLayout()
                .setTitle("Standalone Server")
                .setHeadlineWidget(headline)
                .setDescription("")
                .setTopLevelTools(toolStrip)
                .addContent("Attributes", form.asWidget())
                .addContent("ReloadPanel", reloadPanel);


        return layout.build();
    }

    @Override
    public void setPresenter(StandaloneServerPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateFrom(StandaloneServer server) {
        form.edit(server);
        headline.setText("Server: "+ server.getName());
    }

    @Override
    public void setReloadRequired(boolean reloadRequired) {
        reloadPanel.showWidget( reloadRequired ? 1:0);
    }
}
