/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.graphhopper;

import com.graphhopper.config.CHProfileConfig;
import com.graphhopper.config.LMProfileConfig;
import com.graphhopper.config.ProfileConfig;
import com.graphhopper.routing.util.EncodingManager;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class GraphHopperProfileConfigTest {

    private static final String GH_LOCATION = "target/gh-profile-config-gh";

    @Test
    public void duplicateProfileName_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        assertIllegalArgument("Profile names must be unique. Duplicate name: 'my_profile'", new Runnable() {
            @Override
            public void run() {
                hopper.setProfiles(
                        new ProfileConfig("my_profile").setVehicle("car").setWeighting("fastest"),
                        new ProfileConfig("your_profile").setVehicle("car").setWeighting("short_fastest"),
                        new ProfileConfig("my_profile").setVehicle("car").setWeighting("shortest")
                );
            }
        });
    }

    @Test
    public void vehicleDoesNotExist_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile").setVehicle("your_car"));
        assertIllegalArgument("Unknown vehicle 'your_car' in profile: name=profile", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }

    @Test
    public void vehicleWithoutTurnCostSupport_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile").setVehicle("car").setTurnCosts(true));
        assertIllegalArgument("The profile 'profile' was configured with 'turn_costs=true', but the corresponding vehicle 'car' does not support turn costs", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }

    @Test
    public void profileWithUnknownWeighting_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile").setVehicle("car").setWeighting("your_weighting"));
        assertIllegalArgument("The profile 'profile' was configured with an unknown weighting 'your_weighting'", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }

    @Test
    public void chProfileDoesNotExist_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile1").setVehicle("car"));
        hopper.getCHPreparationHandler().setCHProfileConfigs(new CHProfileConfig("other_profile"));
        assertIllegalArgument("CH profile references unknown profile 'other_profile'", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }

    @Test
    public void duplicateCHProfile_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile").setVehicle("car"));
        hopper.getCHPreparationHandler().setCHProfileConfigs(
                new CHProfileConfig("profile"),
                new CHProfileConfig("profile")
        );
        assertIllegalArgument("Duplicate CH reference to profile 'profile'", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }

    @Test
    public void lmProfileDoesNotExist_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile1").setVehicle("car"));
        hopper.getLMPreparationHandler().setLMProfileConfigs(new LMProfileConfig("other_profile"));
        assertIllegalArgument("LM profile references unknown profile 'other_profile'", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }

    @Test
    public void duplicateLMProfile_error() {
        final GraphHopper hopper = createHopper(EncodingManager.create("car"));
        hopper.setProfiles(new ProfileConfig("profile").setVehicle("car"));
        hopper.getLMPreparationHandler().setLMProfileConfigs(
                new LMProfileConfig("profile"),
                new LMProfileConfig("profile")
        );
        assertIllegalArgument("Duplicate LM reference to profile 'profile'", new Runnable() {
            @Override
            public void run() {
                hopper.load(GH_LOCATION);
            }
        });
    }


    private GraphHopper createHopper(EncodingManager encodingManager) {
        final GraphHopper hopper = new GraphHopper();
        hopper.setGraphHopperLocation(GH_LOCATION);
        hopper.setStoreOnFlush(false);
        hopper.setEncodingManager(encodingManager);
        return hopper;
    }

    private static void assertIllegalArgument(String messagePart, Runnable runnable) {
        try {
            runnable.run();
            fail("There should have been an error containing:\n\t" + messagePart);
        } catch (IllegalArgumentException e) {
            assertTrue("Unexpected error message:\n\t" + e.getMessage() + "\nExpected the message to contain:\n\t" + messagePart, e.getMessage().contains(messagePart));
        }
    }
}