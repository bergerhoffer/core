package org.jboss.as.console.client.shared.subsys.jmx;

import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyCodeSplit;
import com.gwtplatform.mvp.client.proxy.Place;
import com.gwtplatform.mvp.client.proxy.Proxy;
import org.jboss.as.console.client.Console;
import org.jboss.as.console.client.core.NameTokens;
import org.jboss.as.console.client.domain.model.SimpleCallback;
import org.jboss.as.console.client.shared.BeanFactory;
import org.jboss.as.console.client.shared.general.SimpleSuggestion;
import org.jboss.as.console.client.shared.general.SuggestionManagement;
import org.jboss.as.console.client.shared.general.model.LoadSocketBindingsCmd;
import org.jboss.as.console.client.shared.general.model.SocketBinding;
import org.jboss.as.console.client.shared.subsys.Baseadress;
import org.jboss.as.console.client.shared.subsys.RevealStrategy;
import org.jboss.as.console.client.shared.subsys.jmx.model.JMXSubsystem;
import org.jboss.as.console.client.widgets.forms.ApplicationMetaData;
import org.jboss.as.console.client.widgets.forms.BeanMetaData;
import org.jboss.as.console.client.widgets.forms.EntityAdapter;
import org.jboss.as.console.spi.AccessControl;
import org.jboss.as.console.spi.SearchIndex;
import org.jboss.dmr.client.ModelNode;
import org.jboss.dmr.client.Property;
import org.jboss.dmr.client.dispatch.DispatchAsync;
import org.jboss.dmr.client.dispatch.impl.DMRAction;
import org.jboss.dmr.client.dispatch.impl.DMRResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.jboss.dmr.client.ModelDescriptionConstants.*;

/**
 * @author Heiko Braun
 * @date 11/28/11
 */
public class JMXPresenter extends Presenter<JMXPresenter.MyView, JMXPresenter.MyProxy>
    implements SuggestionManagement {

    @ProxyCodeSplit
    @NameToken(NameTokens.JMXPresenter)
    @SearchIndex(keywords = {"jmx", "mbean", "connector", "management"})
    @AccessControl(resources = {"{selected.profile}/subsystem=jmx"}, recursive = false)
    public interface MyProxy extends Proxy<JMXPresenter>, Place {
    }

    public interface MyView extends View {
        void setPresenter(JMXPresenter presenter);
        void updateFrom(JMXSubsystem jpaSubsystem);
    }

    private RevealStrategy revealStrategy;
    private ApplicationMetaData metaData;
    private DispatchAsync dispatcher;
    private EntityAdapter<JMXSubsystem> adapter;
    private BeanMetaData beanMetaData;
    private BeanFactory factory;

    @Inject
    public JMXPresenter(
            EventBus eventBus, MyView view, MyProxy proxy,
            DispatchAsync dispatcher,
            RevealStrategy revealStrategy,
            ApplicationMetaData metaData, BeanFactory factory) {

        super(eventBus, view, proxy);

        this.revealStrategy = revealStrategy;
        this.metaData = metaData;
        this.dispatcher = dispatcher;
        this.beanMetaData = metaData.getBeanMetaData(JMXSubsystem.class);
        this.adapter = new EntityAdapter<JMXSubsystem>(JMXSubsystem.class, metaData);
        this.factory = factory;
    }

    @Override
    protected void onBind() {
        super.onBind();
        getView().setPresenter(this);
    }


    @Override
    protected void onReset() {
        super.onReset();
        loadSubsystem();
    }

    private void loadSubsystem() {

        ModelNode operation = beanMetaData.getAddress().asResource(Baseadress.get());
        operation.get(OP).set(READ_RESOURCE_OPERATION);
        operation.get(RECURSIVE).set(true);

        dispatcher.execute(new DMRAction(operation), new SimpleCallback<DMRResponse>() {
            @Override
            public void onSuccess(DMRResponse result) {
                ModelNode response  = result.get();

                if(response.isFailure())
                {
                    Console.error(Console.MESSAGES.unknown_error(), response.getFailureDescription());
                }
                else
                {
                    ModelNode payload = response.get(RESULT).asObject();
                    JMXSubsystem jmxSubsystem = adapter.fromDMR(payload);


                    if(payload.hasDefined("remoting-connector"))
                    {
                        List<Property> connectorList = payload.get("remoting-connector").asPropertyList();
                        if(!connectorList.isEmpty())
                        {
                            Property item = connectorList.get(0);
                            ModelNode jmxConnector = item.getValue();
                            jmxSubsystem.setMgmtEndpoint(jmxConnector.get("use-management-endpoint").asBoolean());
                        }
                    }

                    getView().updateFrom(jmxSubsystem);
                }
            }
        });
    }

    @Override
    protected void revealInParent() {
        revealStrategy.revealInParent(this);
    }

    public void onSave(final JMXSubsystem editedEntity, Map<String, Object> changeset) {

        List<ModelNode> extraSteps = new ArrayList<ModelNode>(2);

        if(changeset.containsKey("mgmtEndpoint"))
        {
            ModelNode registry = new ModelNode();
            registry.get(ADDRESS).set(Baseadress.get());
            registry.get(ADDRESS).add("subsystem", "jmx");
            registry.get(ADDRESS).add("remoting-connector", "jmx");
            registry.get(OP).set(WRITE_ATTRIBUTE_OPERATION);
            registry.get(NAME).set("use-management-endpoint");
            registry.get(VALUE).set((Boolean)changeset.get("mgmtEndpoint"));

            changeset.remove("mgmtEndpoint");
            extraSteps.add(registry);
        }

        ModelNode operation = adapter.fromChangeset(
                changeset,
                beanMetaData.getAddress().asResource(Baseadress.get()),
                extraSteps.toArray(new ModelNode[] {}));

        dispatcher.execute(new DMRAction(operation), new SimpleCallback<DMRResponse>() {
            @Override
            public void onSuccess(DMRResponse result) {
                ModelNode response  = result.get();

                if(response.isFailure())
                {
                    Console.error(Console.MESSAGES.modificationFailed("JMX Subsystem"), response.getFailureDescription());
                }
                else
                {
                    Console.info(Console.MESSAGES.modified("JMX Subsystem"));
                }

                loadSubsystem();
            }
        });
    }

    @Override
    public void requestSuggestions(final SuggestOracle.Request request, final SuggestOracle.Callback callback) {

        LoadSocketBindingsCmd cmd = new LoadSocketBindingsCmd(dispatcher, factory, metaData);
        cmd.execute("full-ha-sockets", new SimpleCallback<List<SocketBinding>>() {
            @Override
            public void onSuccess(List<SocketBinding> result) {

                List<SimpleSuggestion> suggestions = new ArrayList<SimpleSuggestion>();
                for(SocketBinding binding : result)
                {
                    if(binding.getName().startsWith(request.getQuery()))
                    {
                        SimpleSuggestion suggestion = new SimpleSuggestion(
                                binding.getName(), binding.getName()
                        );
                        suggestions.add(suggestion);
                    }
                }


                SuggestOracle.Response response = new SuggestOracle.Response();
                response.setSuggestions(suggestions);
                response.setMoreSuggestionsCount(suggestions.size());
                callback.onSuggestionsReady(request, response);
            }
        });



    }
}
