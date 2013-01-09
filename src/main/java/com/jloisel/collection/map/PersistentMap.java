package com.jloisel.collection.map;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ForwardingMap;
import com.jloisel.collection.map.externalizer.Externalizer;
import com.jloisel.collection.map.persistence.Persistence;

/**
 * Persistent {@link Map}.
 * {@code null} keys or values are not supported.
 * 
 * Not Thread-safe.
 * 
 * @author jerome
 *
 * @param <V> type of objects being maintained
 */
final class PersistentMap<V extends Persistent> extends ForwardingMap<String, V> {
	private final Map<String, V> delegate;
	private final Externalizer<V> externalizer;
	private final Persistence persistence;

	PersistentMap(final PersistentMapBuilder<V> builder) throws IOException {
		super();
		checkNotNull(builder);
		this.delegate = checkNotNull(builder.delegate);
		this.externalizer = checkNotNull(builder.externalizer);
		this.persistence = checkNotNull(builder.persistence);
		
		for(final String key : persistence) {
			put(key, externalizer.unserialize(persistence.read(key)));
		}
	}

	@Override
	protected Map<String, V> delegate() {
		return delegate;
	}

	@Override
	public V remove(final Object key) {
		try {
			return super.remove(checkNotNull(key));
		} finally {
			try {
				persistence.remove(String.valueOf(key));
			} catch (final IOException e) {
				// cannot be re thrown
			}
		}
	}

	@Override
	public void putAll(final Map<? extends String, ? extends V> map) {
		for (final Entry<? extends String, ? extends V> entry : checkNotNull(map).entrySet()) {
			put(entry.getKey(), entry.getValue());
		}
	}
	
	@Override
	public V put(final String key, final V value) {
		try {
			return super.put(checkNotNull(key), checkNotNull(value));
		} finally {
			try {
				persistence.write(key, externalizer.serialize(value));
			} catch (final IOException e) {
				// cannot be rethrown
			}
		}
	}
	
	@Override
	public void clear() {
		try {
			delegate.clear();
		} finally {
			try {
				persistence.clear();
			} catch (final IOException e) {
				// cannot be rethrown
			}
		}
	}
}