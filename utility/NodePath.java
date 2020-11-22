package com.x.infrastructure.utility;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class NodePath implements Cloneable {
    private String path;
    private NodePath child;


    private NodePath() {

    }

    public static NodePath instance() {
        return new NodePath();
    }

    public static NodePath instance(final String... paths) {
        NodePath nodePath = new NodePath();
        nodePath = nodePath.addNode(paths);
        return nodePath;
    }

    public boolean isLeaf() {
        return child == null;
    }

    public boolean isParent() {
        return child != null;
    }


    public NodePath addNode(final String... paths) {
        NodePath result = null;
        for (String path : paths) {
            NodePath nodePath = NodePath.builder()
                .path(path)
                .build();
            result = (result == null) ? add(this, nodePath) : add(result, nodePath);
        }
        return result;
    }


    private NodePath add(final NodePath current, final NodePath leaf) {
        NodePath result = null;
        try {
            result = (NodePath) current.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        if (result.isParent()) {
            result.setChild(add(result.getChild(), leaf));
        } else if (result.isLeaf() && result.getPath() != null) {
            result.setChild(leaf);
        } else if (result.isLeaf()) {
            result.setPath(leaf.getPath());
        }
        return result;
    }


    public static Builder builder() {
        return new Builder();
    }

    public static class Builder<T> {
        private final NodePath nodePath;

        private Builder() {
            this.nodePath = new NodePath();
        }

        public Builder path(final String path) {
            this.nodePath.setPath(path);
            return this;
        }

        public Builder child(final NodePath child) {
            this.nodePath.setChild(child);
            return this;
        }

        public NodePath build() {
            return this.nodePath;
        }

    }


}
