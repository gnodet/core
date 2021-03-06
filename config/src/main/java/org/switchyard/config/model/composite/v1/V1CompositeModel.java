/* 
 * JBoss, Home of Professional Open Source 
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved. 
 * See the copyright.txt in the distribution for a 
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use, 
 * modify, copy, or redistribute it subject to the terms and conditions 
 * of the GNU Lesser General Public License, v. 2.1. 
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details. 
 * You should have received a copy of the GNU Lesser General Public License, 
 * v.2.1 along with this distribution; if not, write to the Free Software 
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 */

package org.switchyard.config.model.composite.v1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.switchyard.common.property.CompoundPropertyResolver;
import org.switchyard.common.property.SystemAndTestPropertyResolver;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.BaseNamedModel;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.composite.ComponentModel;
import org.switchyard.config.model.composite.CompositeModel;
import org.switchyard.config.model.composite.CompositeReferenceModel;
import org.switchyard.config.model.composite.CompositeServiceModel;
import org.switchyard.config.model.property.PropertyModel;
import org.switchyard.config.model.switchyard.SwitchYardModel;

/**
 * A version 1 CompositeModel.
 *
 * @author David Ward &lt;<a href="mailto:dward@jboss.org">dward@jboss.org</a>&gt; (C) 2011 Red Hat Inc.
 */
public class V1CompositeModel extends BaseNamedModel implements CompositeModel {

    private List<CompositeServiceModel> _services = new ArrayList<CompositeServiceModel>();
    private List<CompositeReferenceModel> _references = new ArrayList<CompositeReferenceModel>();
    private List<ComponentModel> _components = new ArrayList<ComponentModel>();
    private Map<String, PropertyModel> _properties = new HashMap<String, PropertyModel>();
    
    /**
     * Constructs a new V1CompositeModel.
     */
    public V1CompositeModel() {
        super(new QName(CompositeModel.DEFAULT_NAMESPACE, CompositeModel.COMPOSITE));
        setModelChildrenOrder(CompositeServiceModel.SERVICE, CompositeReferenceModel.REFERENCE, ComponentModel.COMPONENT, PropertyModel.PROPERTY);
        setCompositePropertyResolver();
    }

    /**
     * Constructs a new V1CompositeModel with the specified Configuration and Descriptor.
     * @param config the Configuration
     * @param desc the Descriptor
     */
    public V1CompositeModel(Configuration config, Descriptor desc) {
        super(config, desc);
        for (Configuration service_config : config.getChildren(CompositeServiceModel.SERVICE)) {
            CompositeServiceModel service = (CompositeServiceModel)readModel(service_config);
            if (service != null) {
                _services.add(service);
            }
        }
        for (Configuration reference_config : config.getChildren(CompositeReferenceModel.REFERENCE)) {
            CompositeReferenceModel reference = (CompositeReferenceModel)readModel(reference_config);
            if (reference != null) {
                _references.add(reference);
            }
        }
        for (Configuration component_config : config.getChildren(ComponentModel.COMPONENT)) {
            ComponentModel component = (ComponentModel)readModel(component_config);
            if (component != null) {
                _components.add(component);
            }
        }
        for (Configuration property_config : config.getChildren(PropertyModel.PROPERTY)) {
            PropertyModel property = (PropertyModel)readModel(property_config);
            if (property != null) {
                _properties.put(property.getName(), property);
            }
        }
        
        setModelChildrenOrder(CompositeServiceModel.SERVICE, CompositeReferenceModel.REFERENCE, ComponentModel.COMPONENT, PropertyModel.PROPERTY);
        setCompositePropertyResolver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SwitchYardModel getSwitchYard() {
        return (SwitchYardModel)getModelParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<CompositeReferenceModel> getReferences() {
        return Collections.unmodifiableList(_references);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized CompositeModel addReference(CompositeReferenceModel reference) {
        addChildModel(reference);
        _references.add(reference);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<CompositeServiceModel> getServices() {
        return Collections.unmodifiableList(_services);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized CompositeModel addService(CompositeServiceModel service) {
        addChildModel(service);
        _services.add(service);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<ComponentModel> getComponents() {
        return Collections.unmodifiableList(_components);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized CompositeModel addComponent(ComponentModel component) {
        addChildModel(component);
        _components.add(component);
        return this;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void setTargetNamespace(String namespaceUri) {
        this.setModelAttribute(TARGET_NAMESPACE, namespaceUri);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized PropertyModel getProperty(String name) {
        return _properties.get(name);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Map<String, PropertyModel> getProperties() {
        return Collections.unmodifiableMap(_properties);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public V1CompositeModel addProperty(PropertyModel property) {
        addChildModel(property);
        _properties.put(property.getName(), property);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object resolveProperty(String key) {
        Object value = null;
        if (key != null) {
            PropertyModel property = getProperty(key);
            value = property != null ? property.getValue() : null;
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCompositePropertyResolver() {
        Configuration config = getModelConfiguration();
        Configuration parent = config.getParent();
        if (parent != null) {
            config.setPropertyResolver(CompoundPropertyResolver.compact(parent.getPropertyResolver(), this));
        } else {
            config.setPropertyResolver(CompoundPropertyResolver.compact(SystemAndTestPropertyResolver.INSTANCE, this));
        }
        for (ComponentModel component : _components) {
            component.setComponentPropertyResolver();
        }
    }
}
