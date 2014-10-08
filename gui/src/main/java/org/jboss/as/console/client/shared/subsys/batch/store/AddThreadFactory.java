/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.console.client.shared.subsys.batch.store;

import org.jboss.dmr.client.ModelNode;
import org.jboss.gwt.circuit.Action;

/**
 * @author Harald Pehl
 */
public class AddThreadFactory implements Action {

    private final ModelNode threadFactory;

    public AddThreadFactory(ModelNode threadFactory) {
        this.threadFactory = threadFactory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AddThreadFactory)) return false;

        AddThreadFactory that = (AddThreadFactory) o;

        if (!threadFactory.equals(that.threadFactory)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return threadFactory.hashCode();
    }

    public ModelNode getThreadFactory() {
        return threadFactory;
    }
}