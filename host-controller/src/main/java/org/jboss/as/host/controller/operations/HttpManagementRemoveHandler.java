/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.as.host.controller.operations;

import static org.jboss.as.controller.management.BaseHttpInterfaceResourceDefinition.HTTP_MANAGEMENT_RUNTIME_CAPABILITY;

import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ProcessType;
import org.jboss.as.remoting.RemotingHttpUpgradeService;
import org.jboss.as.remoting.RemotingServices;
import org.jboss.as.remoting.management.ManagementRemotingServices;
import org.jboss.as.server.mgmt.UndertowHttpManagementService;
import org.jboss.dmr.ModelNode;

/**
 * Removes the HTTP management interface.
 *
 * @author Brian Stansberry (c) 2011 Red Hat Inc.
 */
public class HttpManagementRemoveHandler extends AbstractRemoveStepHandler {

    private final LocalHostControllerInfoImpl hostControllerInfo;
    private final HttpManagementAddHandler add;

    public HttpManagementRemoveHandler(final LocalHostControllerInfoImpl hostControllerInfo, final HttpManagementAddHandler add) {
        super(HTTP_MANAGEMENT_RUNTIME_CAPABILITY);
        this.hostControllerInfo = hostControllerInfo;
        this.add = add;
    }

    @Override
    protected boolean requiresRuntime(OperationContext context) {
        return (context.getProcessType() != ProcessType.EMBEDDED_HOST_CONTROLLER);
    }

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        context.removeService(UndertowHttpManagementService.SERVICE_NAME);
        context.removeService(UndertowHttpManagementService.SERVICE_NAME.append("requests"));
        context.removeService(UndertowHttpManagementService.SERVICE_NAME.append("shutdown"));

        RemotingServices.removeConnectorServices(context, ManagementRemotingServices.HTTP_CONNECTOR);
        context.removeService(RemotingHttpUpgradeService.UPGRADE_SERVICE_NAME.append(ManagementRemotingServices.HTTP_CONNECTOR));
        clearHostControllerInfo(hostControllerInfo);
    }

    @Override
    protected void recoverServices(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
        add.performRuntime(context, operation, model);
    }

    static void clearHostControllerInfo(LocalHostControllerInfoImpl hostControllerInfo) {
        hostControllerInfo.setHttpManagementInterface(null);
        hostControllerInfo.setHttpManagementPort(0);
        hostControllerInfo.setHttpManagementSecureInterface(null);
        hostControllerInfo.setHttpManagementSecurePort(0);
    }
}
