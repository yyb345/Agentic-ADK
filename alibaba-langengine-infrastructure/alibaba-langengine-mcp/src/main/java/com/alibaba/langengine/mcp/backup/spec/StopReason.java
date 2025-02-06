//package com.alibaba.langengine.mcp.spec;
//
//public interface StopReason {
//
//    String getValue();
//
//    enum KnownReason implements StopReason {
//        EndTurn("endTurn"),
//        StopSequence("stopSequence"),
//        MaxTokens("maxTokens");
//
//        private final String value;
//
//        KnownReason(String value) {
//            this.value = value;
//        }
//
//        @Override
//        public String getValue() {
//            return this.value;
//        }
//    }
//
//    final class Other implements StopReason {
//        private final String value;
//
//        public Other(String value) {
//            this.value = value;
//        }
//
//        @Override
//        public String getValue() {
//            return this.value;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            Other other = (Other) o;
//            return value.equals(other.value);
//        }
//
//        @Override
//        public int hashCode() {
//            return value.hashCode();
//        }
//
//        @Override
//        public String toString() {
//            return "Other(value=" + value + ")";
//        }
//    }
//}
