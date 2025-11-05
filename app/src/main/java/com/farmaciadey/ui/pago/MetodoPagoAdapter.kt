package com.farmaciadey.ui.pago

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.farmaciadey.R
import com.farmaciadey.data.models.MetodoPago

class MetodoPagoAdapter(
    private val onMetodoSelected: (MetodoPago) -> Unit
) : ListAdapter<MetodoPago, MetodoPagoAdapter.MetodoPagoViewHolder>(MetodoPagoDiffCallback()) {
    
    private var selectedPosition = -1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MetodoPagoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_metodo_pago, parent, false)
        return MetodoPagoViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MetodoPagoViewHolder, position: Int) {
        val metodo = getItem(position)
        holder.bind(metodo, position == selectedPosition)
        
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = position
            
            // Actualizar UI
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            
            // Notificar selección
            onMetodoSelected(metodo)
        }
    }
    
    class MetodoPagoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val radioButton: RadioButton = itemView.findViewById(R.id.radioButtonMetodo)
        private val imageViewIcon: ImageView = itemView.findViewById(R.id.imageViewIcon)
        private val textViewTipo: TextView = itemView.findViewById(R.id.textViewTipo)
        private val textViewDescripcion: TextView = itemView.findViewById(R.id.textViewDescripcion)
        
        fun bind(metodo: MetodoPago, isSelected: Boolean) {
            radioButton.isChecked = isSelected
            textViewTipo.text = metodo.displayName
            textViewDescripcion.text = metodo.descripcion ?: "Método de pago disponible"
            
            // Configurar ícono según el tipo
            when (metodo.tipo) {
                "Visa" -> {
                    imageViewIcon.setImageResource(android.R.drawable.ic_secure)
                    itemView.setBackgroundResource(R.drawable.bg_metodo_pago_visa)
                }
                "Yape/Plin" -> {
                    imageViewIcon.setImageResource(android.R.drawable.ic_dialog_email)
                    itemView.setBackgroundResource(R.drawable.bg_metodo_pago_digital)
                }
                else -> {
                    imageViewIcon.setImageResource(android.R.drawable.ic_menu_help)
                    itemView.setBackgroundResource(R.drawable.bg_metodo_pago_default)
                }
            }
            
            // Resaltar si está seleccionado
            if (isSelected) {
                itemView.alpha = 1.0f
                itemView.elevation = 8f
            } else {
                itemView.alpha = 0.8f
                itemView.elevation = 2f
            }
        }
    }
    
    class MetodoPagoDiffCallback : DiffUtil.ItemCallback<MetodoPago>() {
        override fun areItemsTheSame(oldItem: MetodoPago, newItem: MetodoPago): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: MetodoPago, newItem: MetodoPago): Boolean {
            return oldItem == newItem
        }
    }
}
