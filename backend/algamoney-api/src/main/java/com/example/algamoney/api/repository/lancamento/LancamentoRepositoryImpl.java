package com.example.algamoney.api.repository.lancamento;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.util.StringUtils;

import com.example.algamoney.api.model.Lancamento;
import com.example.algamoney.api.model.Lancamento_;
import com.example.algamoney.api.repository.filter.LancamentoFilter;

public class LancamentoRepositoryImpl implements LancamentoRepositoryQuery {

    @PersistenceContext
    private EntityManager manager;

    /**
     * Metodo de filtro usando criteria do JPA.
     * 
     * @param lancamentoFilter - Classe com atributos de filtro de lançamento.
     * @return Retorna uma lista de lançamentos filtrados conforme os valores dos atributos do parâmetro.
     */
    @Override
    public List<Lancamento> filtrar(LancamentoFilter lancamentoFilter) {
        CriteriaBuilder builder = manager.getCriteriaBuilder();
        CriteriaQuery<Lancamento> criteria = builder.createQuery(Lancamento.class);

        Root<Lancamento> root = criteria.from(Lancamento.class);

        // criar as restrições
        Predicate[] predicates = criarRestricoes(lancamentoFilter, builder, root);
        criteria.where(predicates);

        TypedQuery<Lancamento> query = manager.createQuery(criteria);
        return query.getResultList();
    }

    /**
     * Metodo de filtro usando JPQL do JPA.
     * 
     * @param lancamentoFilter - Classe com atributos de filtro de lançamento.
     * @return Retorna uma lista de lançamentos filtrados conforme os valores dos atributos do parâmetro.
     */
    @Override
    public List<Lancamento> filtrarComJpql(LancamentoFilter lancamentoFilter) {
        StringBuilder queryJpql = gerarQueryJpql(lancamentoFilter);

        var createQuery = manager.createQuery(queryJpql.toString(), Lancamento.class);

        setParametersNoCreateQuery(lancamentoFilter, createQuery);

        return createQuery.getResultList();
    }

    private Predicate[] criarRestricoes(LancamentoFilter lancamentoFilter, CriteriaBuilder builder, Root<Lancamento> root) {

        List<Predicate> predicates = new ArrayList<>();

        if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
            predicates.add(builder.like(builder.lower(root.get(Lancamento_.descricao)), "%" + lancamentoFilter.getDescricao().toLowerCase() + "%"));
        }

        if (!StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe())) {
            predicates.add(builder.greaterThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoDe()));
        }

        if (!StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            predicates.add(builder.lessThanOrEqualTo(root.get(Lancamento_.dataVencimento), lancamentoFilter.getDataVencimentoAte()));
        }

        return predicates.toArray(new Predicate[predicates.size()]);
    }

    private void setParametersNoCreateQuery(LancamentoFilter lancamentoFilter, TypedQuery<Lancamento> createQuery) {
        if (!StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe()) && !StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            createQuery.setParameter("dataVencimentoDe", lancamentoFilter.getDataVencimentoDe());
            createQuery.setParameter("dataVencimentoAte", lancamentoFilter.getDataVencimentoAte());
        }

        if (!StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe()) && StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            createQuery.setParameter("dataVencimentoDe", lancamentoFilter.getDataVencimentoDe());
        }

        if (StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe()) && !StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            createQuery.setParameter("dataVencimentoAte", lancamentoFilter.getDataVencimentoAte());
        }

        if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
            createQuery.setParameter("descricao", "%" + lancamentoFilter.getDescricao() + "%");
        }
    }

    private StringBuilder gerarQueryJpql(LancamentoFilter lancamentoFilter) {
        StringBuilder queryJpql = new StringBuilder("from Lancamento ");
        String condicao = "where";

        if (!StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe()) && !StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            queryJpql.append(condicao).append(" dataVencimento between :dataVencimentoDe and :dataVencimentoAte");
            condicao = " and";
        }

        if (!StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe()) && StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            queryJpql.append(condicao).append(" dataVencimento >= :dataVencimentoDe");
            condicao = " and";
        }

        if (StringUtils.isEmpty(lancamentoFilter.getDataVencimentoDe()) && !StringUtils.isEmpty(lancamentoFilter.getDataVencimentoAte())) {
            queryJpql.append(condicao).append(" dataVencimento <= :dataVencimentoAte");
            condicao = " and";
        }

        if (!StringUtils.isEmpty(lancamentoFilter.getDescricao())) {
            queryJpql.append(condicao).append(" descricao LIKE :descricao");
        }
        return queryJpql;
    }

}
