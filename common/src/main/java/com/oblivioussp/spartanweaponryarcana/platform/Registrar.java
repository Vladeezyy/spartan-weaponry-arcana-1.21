package com.oblivioussp.spartanweaponryarcana.platform;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import com.oblivioussp.spartanweaponryarcana.ModSpartanWeaponryArcana;

import com.oblivioussp.spartanweaponry.api.ItemIds;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;

/**
 * The entries of one registry, registered by the loader: NeoForge in its RegisterEvent, Fabric right after the mod
 * starts, registry by registry in the order of {@link BuiltInRegistries#REGISTRY} (the NeoForge order, so the
 * suppliers see the same registered entries on both loaders). Replaces NeoForge's DeferredRegister (the same class as Spartan Weaponry's, with this mod's namespace).
 */
public final class Registrar<R>
{
	private static final List<Registrar<?>> ALL = new ArrayList<>();

	private final ResourceKey<? extends Registry<R>> registry;
	private final List<Entry<R, ? extends R>> entries = new ArrayList<>();

	private Registrar(ResourceKey<? extends Registry<R>> registry)
	{
		this.registry = registry;
	}

	public static <R> Registrar<R> create(ResourceKey<? extends Registry<R>> registry)
	{
		Registrar<R> registrar = new Registrar<>(registry);
		ALL.add(registrar);
		return registrar;
	}

	public <T extends R> Entry<R, T> register(String name, Supplier<T> factory)
	{
		Entry<R, T> entry = new Entry<>(ResourceKey.create(registry, Identifier.fromNamespaceAndPath(ModSpartanWeaponryArcana.ID, name)), factory);
		entries.add(entry);
		return entry;
	}

	public ResourceKey<? extends Registry<R>> registryKey()
	{
		return registry;
	}

	public Collection<Entry<R, ? extends R>> getEntries()
	{
		return Collections.unmodifiableList(entries);
	}

	public static List<Registrar<?>> all()
	{
		return Collections.unmodifiableList(ALL);
	}

	/** Creates the entry's value: the loader calls this once and registers the result. */
	public static <R> R create(Entry<R, ? extends R> entry)
	{
		return entry.create();
	}

	/** A registered entry; {@link #get()} works once the loader has registered it. */
	public static final class Entry<R, T extends R> implements Supplier<T>
	{
		private final ResourceKey<R> key;
		private final Supplier<T> factory;
		private T value;
		private Holder<R> holder;

		private Entry(ResourceKey<R> key, Supplier<T> factory)
		{
			this.key = key;
			this.factory = factory;
		}

		private T create()
		{
			if(value == null)
				// 1.21.2+: item and block properties take the id of the entry (Spartan Weaponry's ItemIds)
				value = ItemIds.creating(key, factory);
			return value;
		}

		@Override
		public T get()
		{
			if(value == null)
				throw new IllegalStateException("Registry entry not registered yet: " + key.identifier());
			return value;
		}

		public boolean isBound()
		{
			return value != null;
		}

		public ResourceKey<R> getKey()
		{
			return key;
		}

		public Identifier getId()
		{
			return key.identifier();
		}

		@SuppressWarnings("unchecked")
		public Holder<R> holder()
		{
			if(holder == null)
			{
				Registry<R> reg = (Registry<R>)BuiltInRegistries.REGISTRY.getValue(key.registry());
				holder = reg.getOrThrow(key);
			}
			return holder;
		}
	}
}
