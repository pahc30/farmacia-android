package com.farmaciadey.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.farmaciadey.databinding.ItemHistorialCompraBinding
import com.farmaciadey.ui.viewmodel.CompraHistorial

class HistorialComprasAdapter(
    private val onCompraClick: (CompraHistorial) -> Unit
) : ListAdapter<CompraHistorial, HistorialComprasAdapter.CompraViewHolder>(CompraHistorialDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompraViewHolder {
        val binding = ItemHistorialCompraBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompraViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompraViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CompraViewHolder(
        private val binding: ItemHistorialCompraBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(compra: CompraHistorial) {
            binding.apply {
                tvTransaccionId.text = "#${compra.transaccionId}"
                tvFecha.text = compra.fecha
                tvMonto.text = "S/ %.2f".format(compra.monto)
                tvMetodoPago.text = compra.metodoPago
                tvEstado.text = compra.estado
                tvProductos.text = compra.productos

                root.setOnClickListener {
                    onCompraClick(compra)
                }
            }
        }
    }
}

class CompraHistorialDiffCallback : DiffUtil.ItemCallback<CompraHistorial>() {
    override fun areItemsTheSame(oldItem: CompraHistorial, newItem: CompraHistorial): Boolean {
        return oldItem.transaccionId == newItem.transaccionId
    }

    override fun areContentsTheSame(oldItem: CompraHistorial, newItem: CompraHistorial): Boolean {
        return oldItem == newItem
    }
}