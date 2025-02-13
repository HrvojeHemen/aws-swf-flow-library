/**
 * Copyright 2012-2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazonaws.services.simpleworkflow.flow.worker;

import software.amazon.awssdk.services.swf.model.Decision;
import software.amazon.awssdk.services.swf.model.DecisionType;
import software.amazon.awssdk.services.swf.model.HistoryEvent;
import software.amazon.awssdk.services.swf.model.ScheduleLambdaFunctionDecisionAttributes;

class LambdaFunctionDecisionStateMachine extends DecisionStateMachineBase {

    private ScheduleLambdaFunctionDecisionAttributes scheduleAttributes;

    LambdaFunctionDecisionStateMachine(DecisionId id, ScheduleLambdaFunctionDecisionAttributes scheduleAttributes) {
        super(id);
        this.scheduleAttributes = scheduleAttributes;
    }

    LambdaFunctionDecisionStateMachine(DecisionId id, ScheduleLambdaFunctionDecisionAttributes scheduleAttributes,
            DecisionState state) {
        super(id, state);
        this.scheduleAttributes = scheduleAttributes;
    }

    @Override
    public Decision getDecision() {
        switch (state) {
        case CREATED:
            return createScheduleLambdaFunctionDecision();
        default:
            return null;
        }
    }

    @Override
    public void handleDecisionTaskStartedEvent() {
        switch (state) {
        case CANCELED_AFTER_INITIATED:
            stateHistory.add("handleDecisionTaskStartedEvent");
            state = DecisionState.CANCELLATION_DECISION_SENT;
            stateHistory.add(state.toString());
            break;
        default:
            super.handleDecisionTaskStartedEvent();
        }
    }

    @Override
    public void handleCancellationFailureEvent(HistoryEvent event) {
        switch (state) {
        case CANCELLATION_DECISION_SENT:
            stateHistory.add("handleCancellationFailureEvent");
            state = DecisionState.INITIATED;
            stateHistory.add(state.toString());
            break;
        default:
            super.handleCancellationFailureEvent(event);
        }
    }

    private Decision createScheduleLambdaFunctionDecision() {
        return Decision.builder()
            .scheduleLambdaFunctionDecisionAttributes(scheduleAttributes)
            .decisionType(DecisionType.SCHEDULE_LAMBDA_FUNCTION).build();
    }

}
