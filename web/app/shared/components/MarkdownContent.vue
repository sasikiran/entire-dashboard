<script setup lang="ts">
const props = defineProps<{
  content: string
}>()
</script>

<template>
  <div class="markdown-content text-sm leading-relaxed">
    <MDC :value="content" tag="div" />
  </div>
</template>

<style scoped>
/* Heading styles (slightly larger than body text, compact overall) */
.markdown-content :deep(h1) {
  font-size: 1.25rem;
  font-weight: 600;
  margin-top: 1.25rem;
  margin-bottom: 0.75rem;
  padding-bottom: 0.375rem;
  border-bottom: 1px solid rgb(229 231 235);
}

.dark .markdown-content :deep(h1) {
  border-bottom-color: rgb(55 65 81);
}

.markdown-content :deep(h2) {
  font-size: 1.125rem;
  font-weight: 600;
  margin-top: 1rem;
  margin-bottom: 0.5rem;
  padding-bottom: 0.375rem;
  border-bottom: 1px solid rgb(229 231 235);
}

.dark .markdown-content :deep(h2) {
  border-bottom-color: rgb(55 65 81);
}

.markdown-content :deep(h3) {
  font-size: 1rem;
  font-weight: 500;
  margin-top: 0.75rem;
  margin-bottom: 0.5rem;
}

.markdown-content :deep(h4) {
  font-size: 0.9375rem;
  font-weight: 500;
  margin-top: 0.75rem;
  margin-bottom: 0.375rem;
}

.markdown-content :deep(h5) {
  font-size: 0.875rem;
  font-weight: 500;
  margin-top: 0.5rem;
  margin-bottom: 0.25rem;
}

.markdown-content :deep(h6) {
  font-size: 0.8125rem;
  font-weight: 500;
  margin-top: 0.5rem;
  margin-bottom: 0.25rem;
}

/* Paragraph styles */
.markdown-content :deep(p) {
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

/* List styles */
.markdown-content :deep(ul) {
  list-style-type: disc;
  list-style-position: inside;
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

.markdown-content :deep(ul li) {
  margin-bottom: 0.25rem;
}

.markdown-content :deep(ol) {
  list-style-type: decimal;
  list-style-position: inside;
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
}

.markdown-content :deep(ol li) {
  margin-bottom: 0.25rem;
}

.markdown-content :deep(li) {
  margin-left: 1rem;
}

.markdown-content :deep(li > ul),
.markdown-content :deep(li > ol) {
  margin-top: 0.25rem;
  margin-bottom: 0.25rem;
}

/* Nested lists */
.markdown-content :deep(ul ul) {
  list-style-type: circle;
}

.markdown-content :deep(ul ul ul) {
  list-style-type: square;
}

/* Blockquote styles */
.markdown-content :deep(blockquote) {
  border-left: 4px solid rgb(209 213 219);
  padding-left: 1rem;
  padding-top: 0.5rem;
  padding-bottom: 0.5rem;
  margin-top: 0.75rem;
  margin-bottom: 0.75rem;
  font-style: italic;
  color: rgb(55 65 81);
  background-color: rgb(249 250 251);
}

.dark .markdown-content :deep(blockquote) {
  border-left-color: rgb(75 85 99);
  color: rgb(209 213 219);
  background-color: rgba(31, 41, 55, 0.5);
}

/* Code styles */
.markdown-content :deep(code) {
  padding: 0.125rem 0.375rem;
  border-radius: 0.25rem;
  font-size: 0.875rem;
  font-family: ui-monospace, monospace;
  background-color: rgb(243 244 246);
  color: rgb(220 38 38);
}

.dark .markdown-content :deep(code) {
  background-color: rgb(31 41 55);
  color: rgb(248 113 113);
}

.markdown-content :deep(pre) {
  margin-top: 1rem;
  margin-bottom: 1rem;
  padding: 1rem;
  border-radius: 0.5rem;
  overflow-x: auto;
  background-color: rgb(249 250 251);
  border: 1px solid rgb(229 231 235);
}

.dark .markdown-content :deep(pre) {
  background-color: rgb(17 24 39);
  border-color: rgb(55 65 81);
}

.markdown-content :deep(pre code) {
  padding: 0;
  background-color: transparent;
  color: inherit;
}

/* Table styles */
.markdown-content :deep(table) {
  width: 100%;
  margin-top: 1rem;
  margin-bottom: 1rem;
  border-collapse: collapse;
}

.markdown-content :deep(thead) {
  background-color: rgb(243 244 246);
}

.dark .markdown-content :deep(thead) {
  background-color: rgb(31 41 55);
}

.markdown-content :deep(th) {
  padding: 0.5rem 1rem;
  text-align: left;
  font-weight: 600;
  border: 1px solid rgb(209 213 219);
}

.dark .markdown-content :deep(th) {
  border-color: rgb(75 85 99);
}

.markdown-content :deep(td) {
  padding: 0.5rem 1rem;
  border: 1px solid rgb(209 213 219);
}

.dark .markdown-content :deep(td) {
  border-color: rgb(75 85 99);
}

.markdown-content :deep(tbody tr:nth-child(even)) {
  background-color: rgb(249 250 251);
}

.dark .markdown-content :deep(tbody tr:nth-child(even)) {
  background-color: rgba(31, 41, 55, 0.5);
}

/* Link styles */
.markdown-content :deep(a) {
  color: var(--color-primary-500);
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

/* Horizontal rule styles */
.markdown-content :deep(hr) {
  margin-top: 1.5rem;
  margin-bottom: 1.5rem;
  border-top: 1px solid rgb(209 213 219);
}

.dark .markdown-content :deep(hr) {
  border-top-color: rgb(75 85 99);
}

/* Emphasis styles */
.markdown-content :deep(strong) {
  font-weight: 700;
}

.markdown-content :deep(em) {
  font-style: italic;
}

/* Strikethrough styles */
.markdown-content :deep(del) {
  text-decoration: line-through;
}

/* Task list styles */
.markdown-content :deep(input[type="checkbox"]) {
  margin-right: 0.5rem;
}

/* Image styles */
.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0.5rem;
  margin-top: 1rem;
  margin-bottom: 1rem;
}

/* Code block language tag */
.markdown-content :deep(.shiki) {
  border-radius: 0.5rem;
}

/* Ensure first element has no top margin */
.markdown-content :deep(> *:first-child) {
  margin-top: 0;
}

/* Ensure last element has no bottom margin */
.markdown-content :deep(> *:last-child) {
  margin-bottom: 0;
}
</style>
