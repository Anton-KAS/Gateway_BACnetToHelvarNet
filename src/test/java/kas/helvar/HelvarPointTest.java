package kas.helvar;

import kas.bacnet.SettableValueToBacnet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HelvarPointTest {

    @ParameterizedTest
    @MethodSource("sourceReadScene")
    public void testGetReadSceneQuery(int group, String expected) {
        //arrange

        //act
        HelvarPoint helvarPoint = new HelvarPoint(null, group, false, "");
        String result = helvarPoint.getReadSceneQuery();

        //assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> sourceReadScene() {
        return Stream.of(
                Arguments.of(1, ">V:1,C:103,G:1,B:1#"),
                Arguments.of(12, ">V:1,C:103,G:12,B:1#"),
                Arguments.of(123, ">V:1,C:103,G:123,B:1#"),
                Arguments.of(1234, ">V:1,C:103,G:1234,B:1#")
        );
    }

    @ParameterizedTest
    @MethodSource("sourceReadConsumption")
    public void testGetReadConsumptionQuery(int group, boolean dimming, String expected) {
        //arrange

        //act
        HelvarPoint helvarPoint = new HelvarPoint(null, group, dimming, "");
        String result = helvarPoint.getReadConsumptionQuery();

        //assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> sourceReadConsumption() {
        return Stream.of(
                Arguments.of(1, true, ">V:1,C:161,G:1#"),
                Arguments.of(12, true, ">V:1,C:161,G:12#"),
                Arguments.of(123, true, ">V:1,C:161,G:123#"),
                Arguments.of(1234, true, ">V:1,C:161,G:1234#"),
                Arguments.of(1234, false, null)
        );
    }

    @ParameterizedTest
    @MethodSource("sourceRecallScene")
    public void testGetRecallSceneQuery(int group, boolean dimming, int sceneNum, String expected) {
        //act
        HelvarPoint helvarPoint = new HelvarPoint(null, group, dimming, "");
        String result = helvarPoint.getRecallSceneQuery(sceneNum);

        //assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> sourceRecallScene() {
        return Stream.of(
                Arguments.of(1, true, -1, ">V:1,C:11,G:1,K:1,B:1,S:1,F:200#"),
                Arguments.of(1, true, 0, ">V:1,C:11,G:1,K:1,B:1,S:1,F:200#"),
                Arguments.of(1, true, 1, ">V:1,C:11,G:1,K:1,B:1,S:1,F:200#"),
                Arguments.of(1, false, 2, ">V:1,C:11,G:1,K:1,B:1,S:2,F:0#"),
                Arguments.of(12, true, 15, ">V:1,C:11,G:12,K:1,B:1,S:15,F:200#"),
                Arguments.of(12, false, 15, ">V:1,C:11,G:12,K:1,B:1,S:15,F:0#"),
                Arguments.of(123, true, 16, ">V:1,C:11,G:123,K:1,B:1,S:16,F:200#"),
                Arguments.of(123, false, 16, ">V:1,C:11,G:123,K:1,B:1,S:16,F:0#"),
                Arguments.of(1234, false, 17, ">V:1,C:11,G:1234,K:1,B:1,S:16,F:0#"),
                Arguments.of(1234, false, 100, ">V:1,C:11,G:1234,K:1,B:1,S:16,F:0#")
        );
    }

    @ParameterizedTest
    @MethodSource("sourceDirectLevel")
    public void testGetDirectLevelQuery(int group, boolean dimming, int directLevelInt, String expected) {
        //act
        HelvarPoint helvarPoint = new HelvarPoint(null, group, dimming, "");
        String result = helvarPoint.getDirectLevelQuery(directLevelInt);

        //assert
        assertEquals(expected, result);
    }

    private static Stream<Arguments> sourceDirectLevel() {
        return Stream.of(
                Arguments.of(1, true, -1, ">V:1,C:13,G:1,L:0,F:200#"),
                Arguments.of(1, true, 0, ">V:1,C:13,G:1,L:0,F:200#"),
                Arguments.of(1, true, 1, ">V:1,C:13,G:1,L:1,F:200#"),
                Arguments.of(1, false, 2, ">V:1,C:13,G:1,L:2,F:0#"),
                Arguments.of(12, true, 50, ">V:1,C:13,G:12,L:50,F:200#"),
                Arguments.of(12, false, 50, ">V:1,C:13,G:12,L:50,F:0#"),
                Arguments.of(123, true, 100, ">V:1,C:13,G:123,L:100,F:200#"),
                Arguments.of(123, false, 100, ">V:1,C:13,G:123,L:100,F:0#"),
                Arguments.of(1234, false, 101, ">V:1,C:13,G:1234,L:100,F:0#"),
                Arguments.of(1234, false, 1000, ">V:1,C:13,G:1234,L:100,F:0#")
        );
    }

    @ParameterizedTest
    @MethodSource("sourceUpdateValue")
    public void testUpdateValue(int group, String type, float value) {
        //arrange
        SettableValueToBacnet valuesToBacnetMock = Mockito.mock(SettableValueToBacnet.class);
        //Mockito.when(helvarPointsMap.setValueFromHelvarNet(host, group, type, value)).thenReturn(true);
        ArgumentCaptor<String> argCaptorType = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> argCaptorGroup = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Float> argCaptorValue = ArgumentCaptor.forClass(Float.class);

        //act
        HelvarPoint helvarPoint = new HelvarPoint(null, group, false, "");
        helvarPoint.updateValue(type, value, valuesToBacnetMock);

        //assert
        Mockito.verify(valuesToBacnetMock, Mockito.times(1)).setValue(type, group, value);

        Mockito.verify(valuesToBacnetMock).setValue(argCaptorType.capture(), argCaptorGroup.capture(), argCaptorValue.capture());
        Assertions.assertEquals(type, argCaptorType.getValue());
        Assertions.assertEquals(group, argCaptorGroup.getValue());
        Assertions.assertEquals(value, argCaptorValue.getValue());
    }

    private static Stream<Arguments> sourceUpdateValue() {
        return Stream.of(
                Arguments.of(1, "av", 3f),
                Arguments.of(2, "av", 2f),
                Arguments.of(3, "av", 1f),
                Arguments.of(1, "ai", 3f),
                Arguments.of(2, "ai", 2f),
                Arguments.of(3, "ai", 1f),
                Arguments.of(1, "ao", 3f),
                Arguments.of(2, "ao", 2f),
                Arguments.of(3, "ao", 1f)
        );
    }
}
