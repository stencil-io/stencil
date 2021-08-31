package io.thestencil.persistence.api;

import java.util.Arrays;
import java.util.List;

public class Serializers {

  public static final List<Class<?>> VALUES = Arrays.asList(
    ImmutableSiteState.class,
    ImmutableArticle.class,
    ImmutableArticleMutator.class,
    ImmutableCreateArticle.class,
    ImmutableCreateLink.class,
    ImmutableCreateLocale.class,
    ImmutableCreatePage.class,
    ImmutableCreateRelease.class,
    ImmutableCreateWorkflow.class,
    ImmutableEntity.class,
    ImmutableLink.class,
    ImmutableLinkArticlePage.class,
    ImmutableLinkMutator.class,
    ImmutableLocale.class,
    ImmutableLocaleMutator.class,
    ImmutablePage.class,
    ImmutablePageMutator.class,
    ImmutableRelease.class,
    ImmutableWorkflow.class,
    ImmutableWorkflowArticlePage.class,
    ImmutableWorkflowMutator.class);
}
