const { DataTypes } = require('sequelize')
const { sequelize } = require('../config/database')

const Convention = sequelize.define('convention', {
    id: {
        type: DataTypes.INTEGER,
        primaryKey: true,
        autoIncrement: true
    },
    date_debut: {
        type: DataTypes.DATE,
        allowNull: false
    },
    date_fin: {
        type: DataTypes.DATE,
        allowNull: false
    },
    valide: {
        type: DataTypes.BOOLEAN,
        defaultValue: false
    },
    pdf_path: {
        type: DataTypes.STRING(500),
        allowNull: true
    }
}, {
    timestamps: true, //created_at
    underscored: true,
    updatedAt: false, //on n'inclut pas updated_at
})

module.exports = Convention