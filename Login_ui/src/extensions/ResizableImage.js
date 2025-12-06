import Image from '@tiptap/extension-image'

export const ResizableImage = Image.extend({
  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: null,
        parseHTML: element => element.getAttribute('width'),
        renderHTML: attributes => {
          if (!attributes.width) {
            return {}
          }
          const width = typeof attributes.width === 'number' 
            ? `${attributes.width}px` 
            : attributes.width
          return {
            width: width,
          }
        },
      },
      height: {
        default: null,
        parseHTML: element => element.getAttribute('height'),
        renderHTML: attributes => {
          if (!attributes.height) {
            return {}
          }
          const height = typeof attributes.height === 'number' 
            ? `${attributes.height}px` 
            : attributes.height
          return {
            height: height,
          }
        },
      },
    }
  },

  addNodeView() {
    return ({ node, HTMLAttributes, getPos, editor }) => {
      const wrapper = document.createElement('div')
      wrapper.className = 'image-wrapper'
      wrapper.style.cssText = 'display: inline-block; position: relative; max-width: 100%; margin: 10px auto;'

      const img = document.createElement('img')
      img.src = node.attrs.src
      img.alt = node.attrs.alt || ''
      
      // 设置默认样式
      img.style.cssText = 'display: block; max-width: 100%; height: auto; cursor: pointer; border-radius: 6px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);'
      
      // 获取原始宽高比
      let aspectRatio = null
      img.onload = () => {
        if (!node.attrs.width && !node.attrs.height) {
          aspectRatio = img.naturalWidth / img.naturalHeight
        }
      }
      
      // 应用尺寸属性
      if (node.attrs.width) {
        const width = typeof node.attrs.width === 'number' 
          ? `${node.attrs.width}px` 
          : node.attrs.width
        img.style.width = width
        // 如果同时设置了宽度和高度，需要移除 height: auto
        if (node.attrs.height) {
          img.style.height = typeof node.attrs.height === 'number' 
            ? `${node.attrs.height}px` 
            : node.attrs.height
        }
      } else if (node.attrs.height) {
        // 只设置了高度
        img.style.height = typeof node.attrs.height === 'number' 
          ? `${node.attrs.height}px` 
          : node.attrs.height
      }
      
      // 确保图片加载错误时也能显示
      img.onerror = () => {
        console.error('图片加载失败:', node.attrs.src)
        img.alt = '图片加载失败'
      }

      // 创建调整大小的控制点
      const resizeHandle = document.createElement('div')
      resizeHandle.className = 'image-resize-handle'
      resizeHandle.setAttribute('contenteditable', 'false')

      wrapper.appendChild(img)
      wrapper.appendChild(resizeHandle)

      // 鼠标悬停显示调整大小控制点
      const handleMouseEnter = () => {
        resizeHandle.style.opacity = '1'
      }
      const handleMouseLeave = () => {
        if (!isResizing) {
          resizeHandle.style.opacity = '0'
        }
      }

      wrapper.addEventListener('mouseenter', handleMouseEnter)
      wrapper.addEventListener('mouseleave', handleMouseLeave)

      // 拖拽调整大小
      let isResizing = false
      let startX = 0
      let startY = 0
      let startWidth = 0
      let startHeight = 0
      let originalAspectRatio = null

      const startResize = (e) => {
        isResizing = true
        startX = e.clientX
        startY = e.clientY
        const rect = img.getBoundingClientRect()
        startWidth = rect.width
        startHeight = rect.height
        originalAspectRatio = startWidth / startHeight
        e.preventDefault()
        e.stopPropagation()
        resizeHandle.style.opacity = '1'
        document.body.style.cursor = 'nwse-resize'
        document.body.style.userSelect = 'none'
      }

      const doResize = (e) => {
        if (!isResizing) return
        
        const deltaX = e.clientX - startX
        const deltaY = e.clientY - startY
        
        // 按住 Shift 键保持宽高比
        const maintainAspectRatio = e.shiftKey && originalAspectRatio
        
        let newWidth = Math.max(50, startWidth + deltaX)
        let newHeight = Math.max(50, startHeight + deltaY)
        
        if (maintainAspectRatio) {
          // 根据宽度计算高度
          newHeight = newWidth / originalAspectRatio
        }
        
        img.style.width = `${newWidth}px`
        img.style.height = `${newHeight}px`
      }

      const stopResize = () => {
        if (!isResizing) return
        isResizing = false
        
        document.body.style.cursor = ''
        document.body.style.userSelect = ''
        
        // 更新节点属性
        const pos = getPos()
        if (pos !== null && pos !== undefined) {
          const rect = img.getBoundingClientRect()
          editor.commands.updateAttributes('image', {
            width: Math.round(rect.width),
            height: Math.round(rect.height),
          })
        }
        
        // 延迟隐藏控制点
        setTimeout(() => {
          if (!wrapper.matches(':hover')) {
            resizeHandle.style.opacity = '0'
          }
        }, 100)
      }

      resizeHandle.addEventListener('mousedown', startResize)
      document.addEventListener('mousemove', doResize)
      document.addEventListener('mouseup', stopResize)

      return {
        dom: wrapper,
        // 图片是 void 节点，不应该有 contentDOM
        destroy: () => {
          wrapper.removeEventListener('mouseenter', handleMouseEnter)
          wrapper.removeEventListener('mouseleave', handleMouseLeave)
          resizeHandle.removeEventListener('mousedown', startResize)
          document.removeEventListener('mousemove', doResize)
          document.removeEventListener('mouseup', stopResize)
        },
      }
    }
  },
})

