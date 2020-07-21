/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jon Peterson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.jonpeterson.jackson.module.versioning

import com.fasterxml.jackson.databind.ObjectMapper
import spock.lang.Specification
import spock.lang.Unroll


class AddedFieldTest extends Specification {

    def mapper = new ObjectMapper().registerModule(new VersioningModule(Vs))

    enum Vs {
        V1, V2, V3
    }

    @JsonVersioned(converterClass = CarConverter)
    static class Car {
        String model
        String make
        int yearMade; // added version 2
        String registrationPlate; // added version 3
        Person owner
        @JsonVersionAttribute
        String _version
        @JsonVersionToAttribute
        String _toVersion
    }

    @JsonVersioned(converterClass = PersonConverter)
    static class Person {
        String firstName
        String lastName
        @JsonVersionAttribute
        String _version
        @JsonVersionToAttribute
        String _toVersion
        String socialSecurityNumber // added version 2
    }

    static class CarConverter extends AbstractVersionConverter<Vs> {
        CarConverter() {
            super(Car.class)
            attributeAdded(Vs.V1, Vs.V2, "yearMade", 2020)
            attributeAdded(Vs.V2, Vs.V3, "registrationPlate", "ABC-123")
        }
    }

    static class PersonConverter extends AbstractVersionConverter<Vs> {
        PersonConverter() {
            super(Person.class)
            attributeAdded(Vs.V1, Vs.V2, "socialSecurityNumber", "1234567890")
        }
    }

    @Unroll
    def 'to past version'() {
        when:
        def car = mapper.readValue('{"model":"camry","make":"toyota","owner":{"firstName":"Per","lastName":"Persson","socialSecurityNumber":"1234567890","_version":"V3"},"registrationPlate":"ABC-123","yearMade":2020,"_version":"V3"}', Car)
        car._toVersion = toVersion
        car.owner._toVersion = toVersion
        def actual = mapper.readValue(mapper.writeValueAsString(car), Map)

        then:
        actual == expected

        where:
        toVersion | expected
        Vs.V1     | [_version: 'V1', make: 'toyota', model: 'camry', 'owner': [_version: 'V1', firstName: 'Per', 'lastName': 'Persson']]
        Vs.V2     | [_version: 'V2', make: 'toyota', model: 'camry', 'yearMade': 2020, 'owner': [_version: 'V2', firstName: 'Per', 'lastName': 'Persson', 'socialSecurityNumber': '1234567890']]
        Vs.V3     | [_version: 'V3', make: 'toyota', model: 'camry', 'yearMade': 2020, 'registrationPlate': 'ABC-123', 'owner': [_version: 'V3', firstName: 'Per', 'lastName': 'Persson', 'socialSecurityNumber': '1234567890']]
    }

    def 'to current version'() {
        when:
        def carV1 = mapper.readValue('{"model":"camry","make":"toyota","owner":{"firstName":"Per","lastName":"Persson","_version":"V1"},"_version":"V1"}', Car)
        def carV3 = mapper.readValue('{"model":"camry","make":"toyota","owner":{"firstName":"Per","lastName":"Persson","socialSecurityNumber":"1234567890","_version":"V3"},"registrationPlate":"ABC-123","yearMade":2020,"_version":"V3"}', Car)
        def actual = mapper.readValue(mapper.writeValueAsString(carV1), Map)
        def expected = mapper.readValue(mapper.writeValueAsString(carV3), Map)

        then:
        actual == expected
    }
}
