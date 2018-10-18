/*
 * Copyright (c) 2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kian.config;

import com.kian.BankAccount;
import com.kian.BankAccountCommandHandler;
import com.kian.BankTransferManagementSaga;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.CommandBusConnector;
import org.axonframework.commandhandling.distributed.CommandRouter;
import org.axonframework.commandhandling.distributed.DistributedCommandBus;
import org.axonframework.config.SagaConfiguration;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.springcloud.commandhandling.SpringCloudCommandRouter;
import org.axonframework.springcloud.commandhandling.SpringHttpCommandBusConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AxonConfig {

    @Autowired
    private AxonConfiguration axonConfiguration;
    @Autowired
    private EventBus eventBus;

    @Bean
    public BankAccountCommandHandler bankAccountCommandHandler() {
        return new BankAccountCommandHandler(axonConfiguration.repository(BankAccount.class), eventBus);
    }

    @Bean
    public SagaConfiguration bankTransferManagementSagaConfiguration() {
        return SagaConfiguration.trackingSagaManager(BankTransferManagementSaga.class);
    }

//    @Autowired
//    public void configure(@Qualifier("localSegment") SimpleCommandBus simpleCommandBus) {
//        simpleCommandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
//    }


    @Bean
    @ConditionalOnMissingBean
    public RestOperations restTemplate() {
        return new RestTemplate();
    }
//    @Autowired
//    public void configure(@Qualifier("localSegment") SimpleCommandBus simpleCommandBus) {
//        simpleCommandBus.registerDispatchInterceptor(new BeanValidationInterceptor<>());
//    }

    @Bean
    public CommandRouter springCloudCommandRouter(DiscoveryClient discoveryClient) {
        return new SpringCloudCommandRouter(discoveryClient, new AnnotationRoutingStrategy());
    }
    @Bean
    public CommandBusConnector springHttpCommandBusConnector(@Qualifier("localSegment") CommandBus localSegment,
                                                             RestOperations restOperations,
                                                             Serializer serializer) {
        return new SpringHttpCommandBusConnector(localSegment, restOperations, serializer);
    }
    @Primary // to make sure this CommandBus implementation is used for autowiring
    @Bean
    public DistributedCommandBus springCloudDistributedCommandBus(CommandRouter commandRouter,
                                                                  CommandBusConnector commandBusConnector) {
        return new DistributedCommandBus(commandRouter, commandBusConnector);
    }

}
