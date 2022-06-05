package kas.helvar;

import org.json.simple.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static kas.helvar.ReceivedObjectProcessor.processing;

public class ReceivedObjectProcessorTest {
    @ParameterizedTest
    @MethodSource("source")
    public void testProcessing(String receiwedMessage, String host, int group, String type, float value, int callNum, boolean returnMock) {
        //arrange
        SetValueFromHelvarNet helvarPointsMap = Mockito.mock(SetValueFromHelvarNet.class);
        Mockito.when(helvarPointsMap.setValueFromHelvarNet(host, group, type, value)).thenReturn(true);
        ArgumentCaptor<String> argCaptorHost = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> argCaptorGroup = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<String> argCaptorValueType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Float> argCaptorValue = ArgumentCaptor.forClass(Float.class);


        //act
        processing(host, receiwedMessage, helvarPointsMap);

        //assert
        Mockito.verify(helvarPointsMap, Mockito.times(callNum)).setValueFromHelvarNet(host, group, type, value);
        if (callNum == 1) {
            Mockito.verify(helvarPointsMap).setValueFromHelvarNet(argCaptorHost.capture(), argCaptorGroup.capture(), argCaptorValueType.capture(), argCaptorValue.capture());
            Assertions.assertEquals(host, argCaptorHost.getValue());
            Assertions.assertEquals(group, argCaptorGroup.getValue());
            Assertions.assertEquals(type, argCaptorValueType.getValue());
            Assertions.assertEquals(value, argCaptorValue.getValue());
        }
    }

    private static Stream<Arguments> source() {
        return Stream.of(
                Arguments.of(">V:1,C:11,G:17,K:1,B:1,S:5,F:90#", "192.168.1.1", 17, "av", 5f, 1, true),
                Arguments.of(">V:1,C:11,G:17,K:1,B:2,S:5,F:90#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:12,B:7,S:4,F:1,@1.2.3.4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:13,G:17,L:60,F:90#", "192.168.1.1", 17, "ao", 60f, 1, true),
                Arguments.of(">V:1,C:14,L:60,F:90,@1.2.3.4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:15,P:72,G:17,B:8,S:16,F:90#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:16,P:72,F:90,@1.2.3.4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:17,P:5,G:17,B:8,S:16,F:90#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:18,P:5,F:90,@1.2.3.4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:19,G:56#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:20,@8.67.2.37#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:21,G:56#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:22,@8.67.2.37#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:23,G:56#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:24,@8.67.2.37#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:101#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:101=1,2,253#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:102,@253#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:102,@253=252,253,254#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:103,G:5,B:1#", "192.168.1.1", 5, null, 0f, 0, false),
                Arguments.of(">V:1,C:103,G:5,B:2#", "192.168.1.1", 5, null, 0f, 0, false),
                Arguments.of("?V:1,C:103,G:5,B:1=4#", "192.168.1.1", 5, "av", 4f, 1, true),
                Arguments.of("?V:1,C:103,G:5,B:2=4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:104,@2.2.1.1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:104,@2.2.1.1=1050626#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">>V:1,C:105,G:5#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:105,G:5=Group 5#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:106,@2.2.1.1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:106,@2.2.1.1=Ballast#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:110,@2.2.1.1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:110,@2.2.1.1=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:111,@1.1.2.58#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:111,@1.1.2.58=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:112,@1.1.2.58#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:112,@1.1.2.58=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:114,@2.2.1.1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:114,@2.2.1.1=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:113,@2.2.1.1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:113,@2.2.1.1=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:129,@1.1.2.58#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:129,@1.1.2.58=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:150,@2.2.1.1.4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:150,@2.2.1.1.4=100#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:151,@2.2.1.1.4#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:151,@2.2.1.1.4=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:152,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:152,@1.1.2.15=25#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:160,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:160,@1.1.2.15=15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:161,G:16#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:161,G:16=105#", "192.168.1.1", 16, "ai", 105f, 1, true),
                Arguments.of(">V:1,C:170,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:170,@1.1.2.15=08:00:00 01-Jul-2009#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:171,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:171,@1.1.2.15=16#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:173,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:173,@1.1.2.15=16#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:174,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:174,@1.1.2.15=40#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:175,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:175,@1.1.2.15=12#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:176,@1.1.2.15#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:176,@1.1.2.15=100#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:185#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:185=1245591399#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:186#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:186=232701#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:187#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:187=232701#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:188#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:188=3600#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:189#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:189=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:190#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:190=67240448#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:191#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:191=1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:201,G:17,O:1,B:2,S:5,L:75#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:202,@2.2.1.1,O:1,B:2,S:5,L:75#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:203,G:17,O:1,B:2,S:5#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:204,@2.2.1.1,O:1,B:2,S:5#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:205,G:56#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:206,@8.67.2.37#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:240,T:1245591399,E:232701,N:185100,Z:3600,Y:1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:241,T:1245591399#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:242,E:232701#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:243,N:185100#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:245,Y:1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of(">V:1,C:104,@:2.2.1.1#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("!V:1,C:104,@:2.2.1.1=11#", "192.168.1.1", 0, null, 0f, 0, false),
                Arguments.of("?V:1,C:103,G:5,B:1=4#", null, 5, "av", 4f, 1, false),
                Arguments.of(">V:1.1,C:11,G:17,K:1,B:1,S:5,F:90#?V:1,C:103,G:17,B:1=5#", "192.168.1.1", 17, "av", 5f, 2, true)
        );
    }

}
