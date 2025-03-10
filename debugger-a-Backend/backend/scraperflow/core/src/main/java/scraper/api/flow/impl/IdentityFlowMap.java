package scraper.api.flow.impl;

import scraper.annotations.NotNull;
import scraper.annotations.Nullable;
import scraper.api.FlowMap;
import scraper.api.L;
import scraper.api.T;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;

public class IdentityFlowMap implements FlowMap {

    @Override
    public <A> void output(@NotNull L<A> locationAndType, @Nullable A outputObject) {

    }

    @Override
    public void forEach(BiConsumer<L<?>, Object> consumer) {

    }

    @NotNull @Override public UUID getId() { throw new IllegalStateException(); }
    @NotNull
    @Override public <A> A eval(@NotNull T<A> template) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public <A> String evalLocation(@NotNull L<A> template) {
        return null;
    }

    @NotNull
    @Override
    public <A> Optional<String> evalLocationMaybe(@NotNull L<A> template) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public <A> Optional<A> evalMaybe(@NotNull T<A> template) {
        return Optional.empty();
    }

    @NotNull
    @Override public <A> A evalIdentity(@NotNull T<A> t) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public <A> Optional<A> evalIdentityMaybe(@NotNull T<A> template) {
        throw new IllegalStateException();
    }

    @NotNull
    @Override
    public FlowMap copy() {
        return null;
    }

    @NotNull
    @Override
    public FlowMap newFlow() {
        return null;
    }

    @Override
    public boolean getSuccessful() {
        return true;
    }

    @Override
    public void setSuccessful(boolean state) {

    }

    public @NotNull ConcurrentMap<String, Object> getMap() { throw new IllegalStateException(); }
    public boolean containsElements(@NotNull final FlowMap expectedOutput) { throw new IllegalStateException(); }

    @NotNull
    @Override
    public Optional<UUID> getParentId() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<Integer> getParentSequence() {
        return Optional.empty();
    }

    @Override
    public int getSequence() {
        return 0;
    }

    @Override
    public void nextSequence() {

    }
}
