package com.farmaciadey.ui.productos

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.farmaciadey.R
import com.farmaciadey.data.models.Producto
import com.farmaciadey.databinding.ItemProductoBinding
import com.farmaciadey.utils.NetworkUtils


class ProductosAdapter(
    private val onAgregarClick: (Producto, Int) -> Unit
) : ListAdapter<Producto, ProductosAdapter.ProductoViewHolder>(ProductoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductoViewHolder {
        val binding = ItemProductoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ProductoViewHolder(
        private val binding: ItemProductoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var cantidad = 1

        fun bind(producto: Producto) {
            binding.apply {
                tvNombre.text = producto.nombre
                tvPrecio.text = String.format("S/ %.2f", producto.precio)
                tvStock.text = "Stock: ${producto.stock}"
                tvCantidad.text = cantidad.toString()
                
                // Cargar imagen con Glide
                if (!producto.url.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(NetworkUtils.convertUrlForEmulator(producto.url))
                        .placeholder(R.drawable.ic_productos)
                        .error(R.drawable.ic_productos)
                        .into(ivProducto)
                } else {
                    ivProducto.setImageResource(R.drawable.ic_productos)
                }
                
                // Configurar botones de cantidad
                btnDecrease.setOnClickListener {
                    if (cantidad > 1) {
                        cantidad--
                        tvCantidad.text = cantidad.toString()
                    }
                }
                
                btnIncrease.setOnClickListener {
                    if (cantidad < producto.stock) {
                        cantidad++
                        tvCantidad.text = cantidad.toString()
                    }
                }
                
                // Click en botón agregar
                btnAgregar.setOnClickListener {
                    onAgregarClick(producto, cantidad)
                }
                
                // Deshabilitar botón si no hay stock
                val hasStock = producto.stock > 0
                btnAgregar.isEnabled = hasStock
                btnIncrease.isEnabled = hasStock && cantidad < producto.stock
                btnDecrease.isEnabled = cantidad > 1
                
                btnAgregar.text = if (hasStock) {
                    "Agregar al Carrito"
                } else {
                    "Sin Stock"
                }
            }
        }
    }
}

class ProductoDiffCallback : DiffUtil.ItemCallback<Producto>() {
    override fun areItemsTheSame(oldItem: Producto, newItem: Producto): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Producto, newItem: Producto): Boolean {
        return oldItem == newItem
    }
}