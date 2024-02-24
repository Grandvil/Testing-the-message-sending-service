package ru.netology;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import ru.netology.entity.Country;
import ru.netology.entity.Location;
import ru.netology.geo.GeoServiceImpl;
import ru.netology.i18n.LocalizationServiceImpl;
import ru.netology.sender.MessageSenderImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.netology.geo.GeoServiceImpl.MOSCOW_IP;
import static ru.netology.geo.GeoServiceImpl.NEW_YORK_IP;

public class MainTests {

    private static long suiteStartTime;
    private long testStartTime;
    private GeoServiceImpl geoService;
    private LocalizationServiceImpl localizationService;

    @BeforeAll
    public static void initSuite() {
        System.out.println("Running StringTest");
        suiteStartTime = System.nanoTime();
    }

    @AfterAll
    public static void completeSuite() {
        System.out.println("StringTest complete: " + (System.nanoTime() - suiteStartTime));
    }

    @BeforeEach
    public void initTest() {
        System.out.println("Starting new nest");
        testStartTime = System.nanoTime();
        geoService = new GeoServiceImpl();
        localizationService = new LocalizationServiceImpl();
    }

    @AfterEach
    public void finalizeTest() {
        System.out.println("\n" + "Test complete: " + (System.nanoTime() - testStartTime));
    }

    @ParameterizedTest
    @MethodSource("sourceOne")
    void testMessageSender(String a, String expected) {
        // act
        geoService = Mockito.mock(GeoServiceImpl.class);
        Mockito.when(geoService.byIp(Mockito.startsWith("172."))).thenReturn(new Location("Moscow", Country.RUSSIA, null, 0));
        Mockito.when(geoService.byIp(Mockito.startsWith("96."))).thenReturn(new Location("New York", Country.USA, null, 0));

        localizationService = Mockito.mock(LocalizationServiceImpl.class);
        Mockito.when(localizationService.locale(Country.RUSSIA)).thenReturn("Добро пожаловать");
        Mockito.when(localizationService.locale(Country.USA)).thenReturn("Welcome");

        MessageSenderImpl messageSender = new MessageSenderImpl(geoService, localizationService);

        Map<String, String> headers = new HashMap<>();
        headers.put(MessageSenderImpl.IP_ADDRESS_HEADER, a);
        String result = messageSender.send(headers);

        // assert
        assertEquals(expected, result);
    }

    public static Stream<Arguments> sourceOne() {
        return Stream.of(
                Arguments.of("172.0.32.11", "Добро пожаловать"),
                Arguments.of("172.1.33.15", "Добро пожаловать"),
                Arguments.of("96.1.432.1", "Welcome"),
                Arguments.of("96.44.183.149", "Welcome"));
    }

    @ParameterizedTest
    @MethodSource("sourceTwo")
    void testByIp(String a, Location expected) {
        // act
        Location result = geoService.byIp(a);

        // assert
        assertThat(expected, samePropertyValuesAs(result));
    }

    public static Stream<Arguments> sourceTwo() {
        return Stream.of(
                Arguments.of(MOSCOW_IP, new Location("Moscow", Country.RUSSIA, "Lenina", 15)),
                Arguments.of(NEW_YORK_IP, new Location("New York", Country.USA, " 10th Avenue", 32)),
                Arguments.of("172.432.432.423", new Location("Moscow", Country.RUSSIA, null, 0)),
                Arguments.of("96.44.183.145", new Location("New York", Country.USA, null, 0)));
    }

    @ParameterizedTest
    @MethodSource("sourceThree")
    void testLocale(Country a, String expected) {
        // act
        String result = localizationService.locale(a);

        // assert
        assertThat(expected, equalTo(result));
    }

    public static Stream<Arguments> sourceThree() {
        return Stream.of(
                Arguments.of(Country.RUSSIA, "Добро пожаловать"),
                Arguments.of(Country.BRAZIL, "Welcome"),
                Arguments.of(Country.USA, "Welcome"),
                Arguments.of(Country.GERMANY, "Welcome"));
    }

    @Test
    void testByCoordinates() {
        assertThrows(RuntimeException.class, () -> geoService.byCoordinates(55.7558, 37.6176));
    }
}
