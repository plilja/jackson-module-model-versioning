/*
 * The MIT License
 * Copyright © 2020 Patrik Lilja
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package se.plilja.jacksonversioning;

import java.lang.reflect.Constructor;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

class ReflectionVersionedConverterRepository<V> implements VersionedConverterRepository<V> {
    private final Map<Class<? extends VersionConverter<V>>, VersionConverter<V>> cache = new ConcurrentHashMap<>();

    @Override
    public VersionConverter<V> get(Class<? extends VersionConverter<V>> converterClass) {
        return cache.computeIfAbsent(converterClass, this::createWithReflection);
    }

    private VersionConverter<V> createWithReflection(Class<? extends VersionConverter<V>> converterClass) {
        if (!converterClass.equals(VersionConverter.class)) {
            try {
                Constructor<? extends VersionConverter<V>> constructor = converterClass.getConstructor();
                return constructor.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("unable to create instance of converter '" + converterClass.getName() + "'", e);
            }
        } else {
            return null;
        }
    }
}
